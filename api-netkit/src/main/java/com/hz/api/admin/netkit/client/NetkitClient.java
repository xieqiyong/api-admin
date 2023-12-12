package com.hz.api.admin.netkit.client;

import com.google.common.collect.Lists;
import com.hz.api.admin.netkit.NetkitFuture;
import com.hz.api.admin.netkit.NetkitPacketHandler;
import com.hz.api.admin.netkit.NoResponseException;
import com.hz.api.admin.netkit.codec.PacketDecoder;
import com.hz.api.admin.netkit.codec.PacketEncoder;
import com.hz.api.admin.netkit.encrypt.EncryptManager;
import com.hz.api.admin.netkit.filter.PacketFilter;
import com.hz.api.admin.netkit.listener.ListenerWrapper;
import com.hz.api.admin.netkit.listener.PacketListener;
import com.hz.api.admin.netkit.listener.ReplyListener;
import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.packet.PingPacket;
import com.hz.api.admin.netkit.packet.ServerErrorPacket;
import com.hz.api.admin.netkit.protocol.ProtocolHeader;
import com.hz.api.admin.netkit.protocol.ProtocolManager;
import com.hz.api.admin.netkit.protocol.netkit.NetkitProtocol;
import com.hz.api.admin.netkit.serialize.SerializationManager;
import com.hz.api.admin.netkit.serialize.fst.FstSerialization;
import com.hz.api.admin.netkit.utils.SystemUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/04/27.
 */
@Slf4j
public class NetkitClient {

	private static final String  defaultProtocol             = SystemUtils.getProp("netkit.client.defaultProtocol", NetkitProtocol.PROTOCOL_NAME);
	private static final String  defaultSerialization        = SystemUtils.getProp("netkit.client.defaultSerialization",
			FstSerialization.SERIALIZATION_NAME);
	private static final long    defaultPacketReplyTimeoutMs = Long.parseLong(
			SystemUtils.getProp("netkit.client.defaultPacketReplyTimeoutMs", "10000"));
	private static final int     defaultHeartbeatTimeSeconds = Integer.parseInt(
			SystemUtils.getProp("netkit.client.defaultHeartbeatTimeSeconds", "3"));
	private static final long    defaultConnectionTimeoutMs  = Long.parseLong(
			SystemUtils.getProp("netkit.client.defaultConnectionTimeoutMs", "5000"));
	private static final boolean defaultReconnectEnable      = Boolean.parseBoolean(
			SystemUtils.getProp("netkit.client.defaultReconnectEnable", "true"));
	private static final long    defaultReconnectIntervalMs  = Long.parseLong(
			SystemUtils.getProp("netkit.client.defaultReconnectIntervalMs", "5000"));
	private static final int     defaultMaxAsyncTasks;

	private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1,
			new DefaultThreadFactory("Netkit Scheduled Executor"));
	// TODO NetkitClient: 线程池需要优化
	private static final ExecutorService          CACHED_EXECUTOR_SERVICE    = Executors.newCachedThreadPool(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("Netkit Cached Executor");
			thread.setDaemon(true);
			thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log.warn(t + " encountered uncaught exception", e);
				}
			});
			return thread;
		}
	});

	static {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		int maxAsyncTasks = (availableProcessors < 8) ? 8 : (int)(availableProcessors * 1.1);
		defaultMaxAsyncTasks = Integer.parseInt(SystemUtils.getProp("netkit.client.defaultMaxAsyncTasks", String.valueOf(maxAsyncTasks)));

		// 加密管理器初始化
		EncryptManager.init();
		// 通信协议初始化
		ProtocolManager.init();
		// 序列化类型初始化
		SerializationManager.init();
	}

	// 连接状态变更监听器
	private final Set<ConnectionListener>                 connectionListeners = new CopyOnWriteArraySet<ConnectionListener>();
	private final Map<PacketListener<?>, ListenerWrapper> recvListeners       = new LinkedHashMap<PacketListener<?>, ListenerWrapper>();
	private final Map<PacketListener<?>, ListenerWrapper> asyncRecvListeners  = new LinkedHashMap<PacketListener<?>, ListenerWrapper>();
	private final AsyncButOrdered<PacketListener<?>>      inOrderListeners    = new AsyncButOrdered<PacketListener<?>>(CACHED_EXECUTOR_SERVICE);
	private final Queue<Runnable>                         deferredAsyncTasks  = new LinkedList<Runnable>();
	private final AtomicBoolean                           started             = new AtomicBoolean(false);

	// 数据通信协议
	private String                  protocol             = defaultProtocol;
	// 数据序列化类型
	private String                  serialization        = defaultSerialization;
	// 是否启用连接断开重连机制
	private boolean                 reconnectEnable      = defaultReconnectEnable;
	// 连接断开重连间隔时间
	private long                    reconnectIntervalMs  = defaultReconnectIntervalMs;
	// 与服务端建立连接超时时间
	private long                    connectionTimeoutMs  = defaultConnectionTimeoutMs;
	// 心跳数据包发送间隔时间
	private long                    heartbeatTimeSeconds = defaultHeartbeatTimeSeconds;
	// 等待数据包返回超时时间
	private long                    packetReplyTimeoutMs = defaultPacketReplyTimeoutMs;
	// 可同时处理的最大异步任务数量
	private int                     maxAsyncTasks        = defaultMaxAsyncTasks;
	// XCenter服务端连接地址
	private List<InetSocketAddress> serverAddresses;

	private int             deferredAsyncTasksCount;
	private int             deferredAsyncTasksCountPrevious;
	private int             currentAsyncTasks;
	private long            lastPacketReceived = 0L;
	private boolean         connected          = false;
	private Bootstrap       bootstrap;
	private Channel channel;
	private ExecutorService channelNotifyExecutor;
	private Timer timer;
	private  InetSocketAddress currentAddress;

	public NetkitClient() {
		connectionListeners.add(new ConnectionListenerImpl());
	}

	public boolean isStarted() {
		return started.get();
	}

	public Channel getChannel() {
		return channel;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getSerialization() {
		return serialization;
	}

	public void setSerialization(String serialization) {
		this.serialization = serialization;
	}

	public boolean isReconnectEnable() {
		return reconnectEnable;
	}

	public void setReconnectEnable(boolean reconnectEnable) {
		this.reconnectEnable = reconnectEnable;
	}

	public long getReconnectIntervalMs() {
		return reconnectIntervalMs;
	}

	public void setReconnectIntervalMs(Long reconnectIntervalMs) {
		Objects.requireNonNull(reconnectIntervalMs, "reconnectIntervalMs must not be null");
		if (reconnectIntervalMs <= 0) {
			throw new IllegalArgumentException("reconnectIntervalMs Must be greater than 0");
		}
		this.reconnectIntervalMs = reconnectIntervalMs;
	}

	public long getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public void setConnectionTimeoutMs(Long connectionTimeoutMs) {
		Objects.requireNonNull(connectionTimeoutMs, "connectionTimeoutMs must not be null");
		if (connectionTimeoutMs <= 0) {
			throw new IllegalArgumentException("connectionTimeoutMs Must be greater than 0");
		}
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public long getHeartbeatTimeSeconds() {
		return heartbeatTimeSeconds;
	}

	public void setHeartbeatTimeSeconds(Long heartbeatTimeSeconds) {
		Objects.requireNonNull(heartbeatTimeSeconds, "heartbeatTimeSeconds must not be null");
		if (heartbeatTimeSeconds <= 0) {
			throw new IllegalArgumentException("heartbeatTimeSeconds Must be greater than 0");
		}
		this.heartbeatTimeSeconds = heartbeatTimeSeconds;
	}

	public long getPacketReplyTimeoutMs() {
		return packetReplyTimeoutMs;
	}

	public void setPacketReplyTimeoutMs(Long packetReplyTimeoutMs) {
		Objects.requireNonNull(packetReplyTimeoutMs, "packetReplyTimeoutMs must not be null");
		if (packetReplyTimeoutMs <= 0) {
			throw new IllegalArgumentException("packetReplyTimeoutMs Must be greater than 0");
		}
		this.packetReplyTimeoutMs = packetReplyTimeoutMs;
	}

	public int getMaxAsyncTasks() {
		return maxAsyncTasks;
	}

	public void setMaxAsyncTasks(int maxAsyncTasks) {
		if (maxAsyncTasks < 1) {
			throw new IllegalArgumentException("maxAsyncTasks must be greater than 0");
		}
		synchronized (deferredAsyncTasks) {
			this.maxAsyncTasks = maxAsyncTasks;
		}
	}

	public List<InetSocketAddress> getServerAddresses() {
		return serverAddresses;
	}

	public void setServerAddresses(List<InetSocketAddress> serverAddresses) {
		this.serverAddresses = serverAddresses;
	}

	public long getLastPacketReceived() {
		return lastPacketReceived;
	}

	public boolean isConnected() {
		return connected;
	}

	public synchronized ChannelFuture connect() {
		if (CollectionUtils.isEmpty(serverAddresses)) {
			throw new IllegalArgumentException("serverAddresses is null");
		}
		return connect(new AddressLoader() {
			@Override
			public InetSocketAddress getAddress() {
				currentAddress = serverAddresses.get(RandomUtils.nextInt(0, serverAddresses.size()));
				return currentAddress;
			}

			@Override
			public InetSocketAddress getRemoveOldAddress() {
				List<InetSocketAddress> list = new ArrayList<InetSocketAddress>(serverAddresses);
				if (currentAddress != null&&(list.size()!=1||currentAddress.equals(list.get(0)))) {
					list.remove(currentAddress);
				}
				if (list.size() == 0) {
					list = serverAddresses;
				}
				currentAddress = list.get(RandomUtils.nextInt(0, list.size()));
				return currentAddress;
			}
		});
	}

	public synchronized ChannelFuture connect(String host, int port) {
		final InetSocketAddress address = new InetSocketAddress(host, port);
		return connect(address);
	}

	public synchronized ChannelFuture connect(final InetSocketAddress address) {
		this.currentAddress = address;
		return connect(new AddressLoader() {
			@Override
			public InetSocketAddress getAddress() {
				return address;
			}

			@Override
			public InetSocketAddress getRemoveOldAddress() {
				return address;
			}
		});
	}

	public synchronized ChannelFuture connect(final AddressLoader addressLoader) {
		if (!started.compareAndSet(false, true)) {
			throw new IllegalStateException("NetkitClient already started");
		}

		final InetSocketAddress serverAddress = addressLoader.getAddress();
		if (serverAddress == null) {
			throw new IllegalArgumentException("serverAddress is null");
		}

		final ConnectionWatchdog.ReconnectListener reconnectListener = new ReconnectListenerImpl();
		final NetkitPacketHandler netkitPacketHandler = new NetkitPacketHandlerImpl();
		final EventLoopGroup boss = new NioEventLoopGroup(0, new DefaultThreadFactory("NetkitClientBoss"));
		timer = new HashedWheelTimer(new DefaultThreadFactory("NetkitHashedWheelTimer"));
		channelNotifyExecutor = Executors.newFixedThreadPool(1, new DefaultThreadFactory("NetkitChannelNotifyExecutor"));
		bootstrap = new Bootstrap();
		bootstrap.group(boss);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		/*bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);*/
		bootstrap.handler(new LoggingHandler(LogLevel.INFO));
		bootstrap.remoteAddress(serverAddress);
		bootstrap.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new IdleStateHandler(0, heartbeatTimeSeconds, 0, TimeUnit.SECONDS));
				if (isReconnectEnable()) {
					p.addLast(new ConnectionWatchdog(bootstrap, addressLoader, timer, Lists.newArrayList(reconnectListener)));
				}
				p.addLast("decoder", new PacketDecoder());
				p.addLast("encoder", new PacketEncoder());
				p.addLast(new ConnectorIdleStateTrigger(NetkitClient.this));
				p.addLast("handler", new NetkitClientHandler(netkitPacketHandler, connectionListeners, channelNotifyExecutor));
			}
		});
		return connect(bootstrap);
	}

	public InetSocketAddress remoteAddress() {
		if (bootstrap != null) {
			return (InetSocketAddress)bootstrap.config().remoteAddress();
		}
		return null;
	}

	private ChannelFuture connect(final Bootstrap bootstrap) {
		ChannelFuture channelFuture = bootstrap.connect();
		channelFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()) {
					log.info("Connect to {} successfully", bootstrap.config().remoteAddress());
				} else {
					log.error("Connect to {} failed", bootstrap.config().remoteAddress(), future.cause());
					if (bootstrap.config().group().isShuttingDown()) {
						return;
					}
					try {
						if (!isReconnectEnable()) {
							return;
						}

						// 连接失败后自动重连
						TimeUnit.MILLISECONDS.sleep(reconnectIntervalMs);
						connect(bootstrap);
					} catch (Exception e) {
						log.error("Reconnect to {} error, Shutting it down...", bootstrap.config().remoteAddress(), e);
						shutdown();
					}
				}
			}
		});
		return channelFuture;
	}

	public void shutdown() {
		log.info("Stopping NetkitClient...");
		if (bootstrap != null) {
			timer.stop();
			if (channel != null) {
				channel.close();
			}
			try {
				bootstrap.config().group().shutdownGracefully().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			channelNotifyExecutor.shutdown();
			SCHEDULED_EXECUTOR_SERVICE.shutdown();
			CACHED_EXECUTOR_SERVICE.shutdown();
		}
	}

	public final ChannelFuture sendPacket(final Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		Objects.requireNonNull(StringUtils.trimToNull(packet.getId()), "Packet.id must not be null");
		Objects.requireNonNull(packet.getType(), "Packet.type must not be null");

		try {
			throwNotConnectedExceptionIfAppropriate();
		} catch (Exception e) {
			log.error("An exception was thrown by sendPacket error, channel=[addr:{}], packet={}, error: {}", channel.remoteAddress(), packet,
					e.getMessage());
			return channel.newFailedFuture(e);
		}

		// 构建协议头
		ProtocolHeader header = new ProtocolHeader();
		header.setProtocol(this.protocol);
		header.setSerialization(this.serialization);
		if (packet instanceof PingPacket) {
			header.setEncrypt(false);
		} else {
			header.setEncrypt(EncryptManager.isEncrypt(channel.id().asLongText()));
		}
		packet.setHeader(header);
		packet.setBornTime(System.currentTimeMillis());
		return this.channel.writeAndFlush(packet).addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Void>>() {
			@Override
			public void operationComplete(Future<? super Void> future) {
				if (!future.isSuccess()) {
					log.error("An exception was thrown by sendPacket error, channel=[addr:{}], packet={}", channel.remoteAddress(), packet,
							future.cause());
				}
			}
		});
	}

	/*public final void sendPacket(Packet packet, long timeout, TimeUnit unit)
			throws NotConnectedException, InterruptedException {
		sendPacket(packet).await(timeout, unit);
	}*/

	public <S extends Packet> NetkitFuture<S, Throwable> sendPacket(S packet, final PacketFilter replyFilter) {
		return sendPacket(packet, replyFilter, getPacketReplyTimeoutMs());
	}

	public <S extends Packet> NetkitFuture<S, Throwable> sendPacket(S packet, final PacketFilter replyFilter, Long replyTimeoutMs) {
		Objects.requireNonNull(packet, "packet must not be null");
		Objects.requireNonNull(replyFilter, "replyFilter must not be null");

		final NetkitFuture.InternalNetkitFuture<S, Throwable> future = new NetkitFuture.InternalNetkitFuture<S, Throwable>();
		sendPacket(packet, replyFilter, new ReplyListener<S>() {
			@Override
			public void onPacket(S packet) {
				future.setResult(packet);
			}

			@Override
			public void onFailure(Throwable e) {
				future.setException(e);
			}
		}, replyTimeoutMs);
		return future;
	}

	public <S extends Packet> void sendPacket(Packet packet, final PacketFilter replyFilter, ReplyListener<S> replyListener) {
		sendPacket(packet, replyFilter, replyListener, getPacketReplyTimeoutMs());
	}

	public <S extends Packet> void sendPacket(Packet packet, final PacketFilter replyFilter, final ReplyListener<S> replyListener,
			Long replyTimeoutMs) {
		Objects.requireNonNull(packet, "packet must not be null");
		Objects.requireNonNull(replyFilter, "replyFilter must not be null");
		Objects.requireNonNull(replyListener, "replyListener must not be null");

		final PacketListener<S> packetListener = new PacketListener<S>() {
			@Override
			public void processPacket(S packet) {
				if (!removeAsyncPacketListener(this)) {
					return;
				}

				if (packet.isError()) {
					replyListener.onFailure(packet.getError());
					return;
				}

				try {
					replyListener.onPacket(packet);
				} catch (Throwable e) {
					replyListener.onFailure(e);
				}
			}
		};

		final long timeoutMs = replyTimeoutMs != null ? replyTimeoutMs : this.packetReplyTimeoutMs;
		SCHEDULED_EXECUTOR_SERVICE.schedule(new Runnable() {
			@Override
			public void run() {
				if (!removeAsyncPacketListener(packetListener)) {
					return;
				}

				if (!isConnected()) {
					replyListener.onFailure(new NotConnectedException(NetkitClient.this, replyFilter));
				} else {
					replyListener.onFailure(NoResponseException.newWith(timeoutMs, replyFilter));
				}
			}
		}, timeoutMs, TimeUnit.MILLISECONDS);

		addAsyncPacketListener(packetListener, replyFilter);
		sendPacket(packet).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (!future.isSuccess()) {
					if (removeAsyncPacketListener(packetListener)) {
						replyListener.onFailure(future.cause());
					}
				}
			}
		});
	}

	public Set<ConnectionListener> getConnectionListeners() {
		return connectionListeners;
	}

	public void addConnectionListener(ConnectionListener connectionListener) {
		if (connectionListener == null) {
			return;
		}
		connectionListeners.add(connectionListener);
	}

	public void removeConnectionListener(ConnectionListener connectionListener) {
		connectionListeners.remove(connectionListener);
	}

	// ********************************** Listener ********************************** //

	public void addPacketListener(PacketListener<?> packetListener, PacketFilter packetFilter) {
		if (packetListener == null) {
			throw new NullPointerException("Given packet listener must not be null");
		}
		ListenerWrapper wrapper = new ListenerWrapper(packetListener, packetFilter);
		synchronized (recvListeners) {
			recvListeners.put(packetListener, wrapper);
		}
	}

	public boolean removePacketListener(PacketListener<?> packetListener) {
		synchronized (recvListeners) {
			return recvListeners.remove(packetListener) != null;
		}
	}

	public void addAsyncPacketListener(PacketListener<?> packetListener, PacketFilter packetFilter) {
		if (packetListener == null) {
			throw new NullPointerException("Packet listener is null.");
		}
		ListenerWrapper wrapper = new ListenerWrapper(packetListener, packetFilter);
		synchronized (asyncRecvListeners) {
			asyncRecvListeners.put(packetListener, wrapper);
		}
	}

	public boolean removeAsyncPacketListener(PacketListener<?> packetListener) {
		synchronized (asyncRecvListeners) {
			return asyncRecvListeners.remove(packetListener) != null;
		}
	}

	// TODO 日志完善
	void invokeNotifyRecvListeners(final Packet packet) {
		if (packet instanceof ServerErrorPacket) {
			log.error("ServerError: {}", packet);
			return;
		}

		final Collection<PacketListener<?>> listenersToNotify = new LinkedList<PacketListener<?>>();
		synchronized (asyncRecvListeners) {
			for (ListenerWrapper listenerWrapper : asyncRecvListeners.values()) {
				if (listenerWrapper.filterMatches(packet)) {
					listenersToNotify.add(listenerWrapper.getListener());
				}
			}
		}
		//noinspection rawtypes
		for (final PacketListener listener : listenersToNotify) {
			asyncGoLimited(new Runnable() {
				@Override
				public void run() {
					try {
						//noinspection unchecked
						listener.processPacket(packet);
					} catch (Throwable e) {
						log.error("An exception was thrown by {}.processPacket error, channel=[addr:{}], packet={}", listener.getClass().getName(),
								channel.remoteAddress(), packet, e);
					}
				}
			});
		}

		listenersToNotify.clear();
		synchronized (recvListeners) {
			for (ListenerWrapper listenerWrapper : recvListeners.values()) {
				if (listenerWrapper.filterMatches(packet)) {
					listenersToNotify.add(listenerWrapper.getListener());
				}
			}
		}
		//noinspection rawtypes
		for (final PacketListener listener : listenersToNotify) {
			inOrderListeners.performAsyncButOrdered(listener, new Runnable() {
				@Override
				public void run() {
					try {
						//noinspection unchecked
						listener.processPacket(packet);
					} catch (Throwable e) {
						log.error("An exception was thrown by {}.processPacket error, channel=[addr:{}], packet={}", listener.getClass().getName(),
								channel.remoteAddress(), packet, e);
					}
				}
			});
		}

		// Notify the receive listeners interested in the packet
		listenersToNotify.clear();
	}

	protected void asyncGoLimited(final Runnable runnable) {
		Runnable wrappedRunnable = new Runnable() {
			@Override
			public void run() {
				runnable.run();
				synchronized (deferredAsyncTasks) {
					Runnable defferredRunnable = deferredAsyncTasks.poll();
					if (defferredRunnable == null) {
						currentAsyncTasks--;
					} else {
						deferredAsyncTasksCount--;
						CACHED_EXECUTOR_SERVICE.execute(defferredRunnable);
					}
				}
			}
		};

		synchronized (deferredAsyncTasks) {
			if (currentAsyncTasks < maxAsyncTasks) {
				currentAsyncTasks++;
				CACHED_EXECUTOR_SERVICE.execute(wrappedRunnable);
			} else {
				deferredAsyncTasksCount++;
				deferredAsyncTasks.add(wrappedRunnable);
			}

			final int HIGH_WATERMARK = 100;
			final int INFORM_WATERMARK = 20;
			final int deferredAsyncRunnablesCount = this.deferredAsyncTasksCount;

			if (deferredAsyncRunnablesCount >= HIGH_WATERMARK && deferredAsyncTasksCountPrevious < HIGH_WATERMARK) {
				log.warn("High watermark of " + HIGH_WATERMARK + " simultaneous executing runnables reached");
			} else if (deferredAsyncRunnablesCount >= INFORM_WATERMARK && deferredAsyncTasksCountPrevious < INFORM_WATERMARK) {
				log.info(INFORM_WATERMARK + " simultaneous executing runnables reached");
			}
			deferredAsyncTasksCountPrevious = deferredAsyncRunnablesCount;
		}
	}

	// ********************************** processPacket ********************************** //

	private void throwNotConnectedExceptionIfAppropriate() throws NotConnectedException {
		if (!isConnected()) {
			throw new NotConnectedException();
		}
	}

	class ConnectionListenerImpl extends ConnectionListenerAdapter {

		@Override
		public void channelConnected(Channel channel) {
			NetkitClient.this.channel = channel;
			NetkitClient.this.connected = true;
		}

		@Override
		public void channelClosed(Channel channel) {
			NetkitClient.this.connected = false;
		}
	}

	class ReconnectListenerImpl implements ConnectionWatchdog.ReconnectListener {

		@Override
		public void reconnected(Channel channel) {
			for (ConnectionListener listener : getConnectionListeners()) {
				try {
					listener.channelReconnected(channel);
				} catch (Throwable e) {
					log.error("An exception was thrown by {}.channelReconnected error", listener.getClass().getName(), e);
				}
			}
		}
	}

	class NetkitPacketHandlerImpl implements NetkitPacketHandler {

		@Override
		public void processPacket(final Packet packet) {
			lastPacketReceived = System.currentTimeMillis();
			// // Deliver the incoming packet to listeners.
			invokeNotifyRecvListeners(packet);
		}
	}
}
