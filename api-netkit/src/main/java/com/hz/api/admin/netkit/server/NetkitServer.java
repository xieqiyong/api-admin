package com.hz.api.admin.netkit.server;

import com.hz.api.admin.netkit.codec.PacketDecoder;
import com.hz.api.admin.netkit.codec.PacketEncoder;
import com.hz.api.admin.netkit.encrypt.EncryptManager;
import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.protocol.ProtocolManager;
import com.hz.api.admin.netkit.serialize.SerializationManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GlobalTrafficShapingHandler
 * ChannelTrafficShapingHandler
 * GlobalChannelTrafficShapingHandler
 * FlowControlHandler
 * <p>
 * Netty系列文章：https://www.jianshu.com/nb/31795191
 * Netty流控和流量整形: https://www.jianshu.com/p/6c4a7cbbe2b5
 * Netty业务处理中线程池和内存池的使用: https://www.jianshu.com/p/2c3f2253e30c
 * 性能指标采集监控: https://www.jianshu.com/p/4c3b5d012ed1
 * Netty事件触发: https://www.jianshu.com/p/85667a891235
 * IoT百万长连接性能调优: https://www.jianshu.com/p/54f9bfcd054b
 * Netty源码分析-拆包器之LengthFieldBasedFrameDecoder: https://www.cnblogs.com/java-chen-hao/p/11571229.html#_label0
 * Netty变长协议: http://www.tianshouzhi.com/api/tutorials/netty/398 http://www.tianshouzhi.com/api/tutorials/netty/398
 *
 * @author zhangzxiang91@gmail.com
 * @date 2021/04/27.
 */
@Slf4j
public class NetkitServer {

	static {
		// 加密管理器初始化
		EncryptManager.init();
		// 通信协议初始化
		ProtocolManager.init();
		// 序列化类型初始化
		SerializationManager.init();
	}

	private final AtomicBoolean                                started  = new AtomicBoolean(false);
	private final Map<String, PacketHandler<? extends Packet>> handlers = new HashMap<String, PacketHandler<? extends Packet>>();

	private long    channelReadTimeoutMs = 5000;
	private String  host                 = "0.0.0.0";
	private Integer port;

	private ServerBootstrap   bootstrap;
	private Channel channel;
	private ChannelGroup      channels;
	private ConnectionManager connectionManager;

	public NetkitServer(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public long getChannelReadTimeoutMs() {
		return channelReadTimeoutMs;
	}

	public void setChannelReadTimeoutMs(long channelReadTimeoutMs) {
		this.channelReadTimeoutMs = channelReadTimeoutMs;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public ServerBootstrap getBootstrap() {
		return bootstrap;
	}

	public Channel getChannel() {
		return channel;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public Map<String, PacketHandler<? extends Packet>> getPacketHandlers() {
		return handlers;
	}

	public void addPacketHandler(PacketHandler<? extends Packet> handler) {
		handlers.put(handler.getName(), handler);
	}

	public synchronized void start() {
		if (!started.compareAndSet(false, true)) {
			throw new IllegalStateException("NetkitServer already started");
		}

		EventLoopGroup boss = new NioEventLoopGroup(1, new DefaultThreadFactory("NetkitServerBoss"));
		EventLoopGroup worker = new NioEventLoopGroup(0, new DefaultThreadFactory("NetkitServerWorker"));

		channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		bootstrap = new ServerBootstrap();
		bootstrap.group(boss, worker);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
		/*bootstrap.handler(new LoggingHandler(LogLevel.ERROR));*/
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) {
				ChannelPipeline p = ch.pipeline();
				p.addLast(new ReadTimeoutHandler(channelReadTimeoutMs, TimeUnit.MILLISECONDS));
				p.addLast("decoder", new PacketDecoder());
				p.addLast("encoder", new PacketEncoder());
				p.addLast("handler", new NetkitServerHandler(NetkitServer.this, channels));
			}
		});
		ChannelFuture future = bootstrap.bind(new InetSocketAddress(host, port));
		future.syncUninterruptibly();
		channel = future.channel();

		for (PacketHandler handler : handlers.values()) {
			handler.init(this);
		}
	}

	public void shutdown() {
		if (channel != null) {
			channel.close();
		}
		if (bootstrap != null) {
			bootstrap.config().group().shutdownGracefully();
			bootstrap.config().childGroup().shutdownGracefully();
		}
		if (channels != null) {
			channels.close();
		}
	}
}
