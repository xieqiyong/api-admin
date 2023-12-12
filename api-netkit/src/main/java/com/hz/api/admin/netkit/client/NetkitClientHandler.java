/*
 * Created by zhangzxiang91@gmail.com on 2021/05/07.
 */
package com.hz.api.admin.netkit.client;

import com.google.common.base.Optional;
import com.hz.api.admin.netkit.NetkitPacketHandler;
import com.hz.api.admin.netkit.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/07.
 */
@Slf4j
public class NetkitClientHandler extends SimpleChannelInboundHandler<Packet> {

	private final NetkitPacketHandler handler;
	private final Set<ConnectionListener> connectionListeners;
	private final ExecutorService         executorService;

	public NetkitClientHandler(NetkitPacketHandler handler, Set<ConnectionListener> connectionListeners, ExecutorService executorService) {
		Objects.requireNonNull(handler, "NetkitPacketHandler must not be null");
		this.handler = handler;
		this.connectionListeners = Optional.fromNullable(connectionListeners).or(new HashSet<ConnectionListener>());
		this.executorService = executorService;
	}

	/**
	 * 连接建立
	 */
	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("channelActive - {}", ctx.channel().remoteAddress());
		}
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners) {
					try {
						listener.channelConnected(ctx.channel());
					} catch (Throwable e) {
						log.warn("An exception was thrown by {}.channelConnected() error: {}", listener.getClass().getName(), e);
					}
				}
			}
		});
		super.channelActive(ctx);
	}

	/**
	 * 连接断开
	 */
	@Override
	public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("channelInactive - {}", ctx.channel().remoteAddress());
		}
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners) {
					try {
						listener.channelClosed(ctx.channel());
					} catch (Throwable e) {
						log.warn("An exception was thrown by {}.channelClosed() error: {}", listener.getClass().getName(), e.getMessage());
					}
				}
			}
		});
		super.channelInactive(ctx);
	}

	/**
	 * 异常处理
	 */
	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
		// TODO exceptionCaught: 优化异常栈日志输出
		log.error("exceptionCaught - {}", ctx.channel().remoteAddress(), cause);
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				for (ConnectionListener listener : connectionListeners) {
					try {
						listener.exceptionCaught(ctx.channel(), cause);
					} catch (Throwable e) {
						log.warn("An exception was thrown by {}.exceptionCaught() error: {}", listener.getClass().getName(), e.getMessage());
					}
				}
			}
		});
		super.exceptionCaught(ctx, cause);
	}

	/**
	 * 接收消息
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("channelRead0 - {} packet: {}", ctx.channel().remoteAddress(), msg);
		}
		handler.processPacket(msg);
	}
}
