package com.hz.api.admin.netkit.server;

import com.hz.api.admin.netkit.NetkitFuture;
import com.hz.api.admin.netkit.NoResponseException;
import com.hz.api.admin.netkit.filter.PacketFilter;
import com.hz.api.admin.netkit.listener.ListenerWrapper;
import com.hz.api.admin.netkit.listener.PacketListener;
import com.hz.api.admin.netkit.listener.ReplyListener;
import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.protocol.ProtocolHeader;
import com.hz.api.admin.netkit.utils.SystemUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/31.
 */
@Slf4j
public class NetkitConnection {

	private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);

	// 等待数据包返回超时时间
	private static final long defaultPacketReplyTimeoutMs = Long.parseLong(SystemUtils.getProp("netkit.client.defaultPacketReplyTimeoutMs", "10000"
	));

	private final Map<ConnectionCloseListener<Object>, Object> closeListeners       = new HashMap<ConnectionCloseListener<Object>, Object>();
	// private final        AtomicLong                              clientPacketCount           = new AtomicLong(0);
	// private final        AtomicLong                              serverPacketCount           = new AtomicLong(0);
	private final AtomicReference<Status>                      status               = new AtomicReference<Status>(Status.CONNECTED);
	private final Channel                                      channel;
	private final long                                         creationTime         = System.currentTimeMillis();
	private final Map<PacketListener<?>, ListenerWrapper>      replyRecvListeners   = new LinkedHashMap<PacketListener<?>, ListenerWrapper>();
	// private       long                                         lastActiveTime;
	private       long                                         packetReplyTimeoutMs = defaultPacketReplyTimeoutMs;
	private       ExecutorService                              executor;
	private       String                                       channelId;
	private ProtocolHeader protocolHeader;

	public NetkitConnection(Channel channel, ExecutorService executor) {
		this.channel = channel;
		this.channelId = channel.id().asLongText();
		this.executor = executor;
	}

	public void bindProtocolHeader(ProtocolHeader protocolHeader, boolean encrypt) {
		ProtocolHeader header = new ProtocolHeader();
		header.setProtocol(protocolHeader.getProtocol());
		header.setSerialization(protocolHeader.getSerialization());
		header.setEncrypt(encrypt);
		this.protocolHeader = header;
	}

	public ProtocolHeader getProtocolHeader() {
		return protocolHeader;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getChannelId() {
		return channelId;
	}

	public Date getCreationTime() {
		return new Date(creationTime);
	}

	/*public Date getLastActiveTime() {
		return new Date(lastActiveTime);
	}*/

	/*public void incrementClientPacketCount() {
		clientPacketCount.incrementAndGet();
		lastActiveTime = System.currentTimeMillis();
	}

	public void incrementServerPacketCount() {
		serverPacketCount.incrementAndGet();
		lastActiveTime = System.currentTimeMillis();
	}*/

	public byte[] getAddress() {
		final SocketAddress remoteAddress = channel.remoteAddress();
		final InetSocketAddress socketAddress = (InetSocketAddress)remoteAddress;
		final InetAddress address = socketAddress.getAddress();
		return address.getAddress();
	}

	public String getHostAddress() {
		final SocketAddress remoteAddress = channel.remoteAddress();
		final InetSocketAddress socketAddress = (InetSocketAddress)remoteAddress;
		final InetAddress inetAddress = socketAddress.getAddress();
		return inetAddress.getHostAddress();
	}

	public String getHostName() {
		final SocketAddress remoteAddress = channel.remoteAddress();
		final InetSocketAddress socketAddress = (InetSocketAddress)remoteAddress;
		final InetAddress inetAddress = socketAddress.getAddress();
		return inetAddress.getHostName();
	}

	public void setStatus(Status state) {
		this.status.set(state);
	}

	public boolean isConnected() {
		return status.get() == Status.CONNECTED;
	}

	public boolean isClosed() {
		return status.get() == Status.CLOSED;
	}

	public void close() {
		if (status.compareAndSet(Status.CONNECTED, Status.CLOSED)) {
			try {
				channel.close();
			} catch (Exception e) {
				log.error("Exception while closing Netty channel", e);
			}
			notifyCloseListeners();
			closeListeners.clear();
		}
	}

	private void notifyCloseListeners() {
		for (final Map.Entry<ConnectionCloseListener<Object>, Object> entry : closeListeners.entrySet()) {
			if (entry.getKey() != null) {
				try {
					entry.getKey().onConnectionClose(entry.getValue());
				} catch (Throwable e) {
					log.error("Error notifying listener: " + entry.getKey(), e);
				}
			}
		}
	}

	public <T> void registerCloseListener(ConnectionCloseListener<T> listener, T callback) {
		if (isClosed()) {
			listener.onConnectionClose(callback);
		} else {
			//noinspection unchecked
			closeListeners.put((ConnectionCloseListener<Object>)listener, callback);
		}
	}

	public void removeCloseListener(ConnectionCloseListener<?> listener) {
		closeListeners.remove(listener);
	}

	public final ChannelFuture sendPacket(final Packet packet) {
		Objects.requireNonNull(packet, "Packet must not be null");
		Objects.requireNonNull(StringUtils.trimToNull(packet.getId()), "Packet.id must not be null");
		Objects.requireNonNull(packet.getType(), "Packet.type must not be null");

		if (isClosed()) {
			return channel.newFailedFuture(new ConnectionClosedException(this));
		}

		if (packet.getHeader() != null) {
			packet.setHeader(packet.getHeader());
		} else {
			if (protocolHeader == null) {
				return channel.newFailedFuture(new IllegalArgumentException("ProtocolHeader is null"));
			}
			packet.setHeader(protocolHeader);
		}
		packet.setBornTime(System.currentTimeMillis());
		return this.channel.writeAndFlush(packet).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (!future.isSuccess()) {
					log.error("An exception was thrown by sendPacket error, channel=[id:{} addr:{}], packet={}", channel.id(),
							channel.remoteAddress(), packet, future.cause());
				}
			}
		});
	}

	public <S extends Packet> NetkitFuture<S, Throwable> sendPacket(S packet, final PacketFilter replyFilter) {
		return sendPacket(packet, replyFilter, packetReplyTimeoutMs);
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
		sendPacket(packet, replyFilter, replyListener, packetReplyTimeoutMs);
	}

	public <S extends Packet> void sendPacket(Packet packet, final PacketFilter replyFilter, final ReplyListener<S> replyListener,
			Long replyTimeoutMs) {
		Objects.requireNonNull(packet, "packet must not be null");
		Objects.requireNonNull(replyFilter, "replyFilter must not be null");
		Objects.requireNonNull(replyListener, "replyListener must not be null");

		if (isClosed()) {
			replyListener.onFailure(new ConnectionClosedException(this, replyFilter));
			return;
		}

		final PacketListener<S> packetListener = new PacketListener<S>() {
			@Override
			public void processPacket(S packet) {
				if (!removeReplyListener(this)) {
					return;
				}

				if (packet.getError() != null) {
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
				if (!removeReplyListener(packetListener)) {
					return;
				}

				if (isClosed()) {
					replyListener.onFailure(new ConnectionClosedException(NetkitConnection.this, replyFilter));
				} else {
					replyListener.onFailure(NoResponseException.newWith(timeoutMs, replyFilter));
				}
			}
		}, timeoutMs, TimeUnit.MILLISECONDS);

		addReplyListener(packetListener, replyFilter);
		sendPacket(packet).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (!future.isSuccess()) {
					if (removeReplyListener(packetListener)) {
						replyListener.onFailure(future.cause());
					}
				}
			}
		});
	}

	private void addReplyListener(PacketListener<?> listener, PacketFilter filter) {
		if (listener == null) {
			throw new NullPointerException("Packet listener is null.");
		}
		ListenerWrapper wrapper = new ListenerWrapper(listener, filter);
		synchronized (replyRecvListeners) {
			replyRecvListeners.put(listener, wrapper);
		}
	}

	private boolean removeReplyListener(PacketListener<?> packetListener) {
		synchronized (replyRecvListeners) {
			return replyRecvListeners.remove(packetListener) != null;
		}
	}

	public void processPacket(final Packet packet) {
		// lastPacketReceived = System.currentTimeMillis();
		final List<PacketListener<?>> listeners = findMatchingListeners(packet);
		//noinspection rawtypes
		for (final PacketListener listener : listeners) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						//noinspection unchecked
						listener.processPacket(packet);
					} catch (Throwable e) {
						log.error("An exception was thrown by {}.processPacket error, channel=[id:{} addr:{}], packet={}",
								listener.getClass().getName(), channel.id(), channel.remoteAddress(), packet, e);
					}
				}
			});
		}
	}

	private List<PacketListener<?>> findMatchingListeners(Packet packet) {
		List<PacketListener<?>> listenersToNotify = new LinkedList<PacketListener<?>>();
		for (ListenerWrapper listenerWrapper : replyRecvListeners.values()) {
			if (listenerWrapper.filterMatches(packet)) {
				listenersToNotify.add(listenerWrapper.getListener());
			}
		}
		return listenersToNotify;
	}

	enum Status {CONNECTED, CLOSED}
}
