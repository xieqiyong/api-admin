/*
 * Created by zhangzxiang91@gmail.com on 2020/11/04.
 */
package com.hz.api.admin.netkit.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.google.common.base.Optional;

/**
 * 处理连接断开重连
 *
 * @author zhangzxiang91@gmail.com
 * @date 2020/11/04.
 */
@Slf4j
public class ConnectionWatchdog extends ChannelInboundHandlerAdapter {

	private static final int BACKOFF_CAP = 12;

	private final Bootstrap               bootstrap;
	private final AddressLoader           addressLoader;
	private final Timer                   timer;
	private final boolean                 fastReconnect;
	private final List<ReconnectListener> listeners;

	public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, List<ReconnectListener> listeners) {
		this(bootstrap, null, timer, false, listeners);
	}

	public ConnectionWatchdog(Bootstrap bootstrap, AddressLoader addressLoader, Timer timer, List<ReconnectListener> listeners) {
		this(bootstrap, addressLoader, timer, false, listeners);
	}

	public ConnectionWatchdog(Bootstrap bootstrap, AddressLoader addressLoader, Timer timer, boolean fastReconnect,
			List<ReconnectListener> listeners) {
		this.bootstrap = bootstrap;
		this.addressLoader = addressLoader;
		this.timer = timer;
		this.fastReconnect = fastReconnect;
		this.listeners = Optional.fromNullable(listeners).or(new ArrayList<ReconnectListener>());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (fastReconnect) {
			tryReconnect(1);
		} else {
			reconnect(1);
		}
		ctx.fireChannelInactive();
	}

	private void reconnect(final int attempts) {
		if (bootstrap.config().group().isShuttingDown()) {
			return;
		}

		try {
			int timeout = 2 << attempts;
			timer.newTimeout(new TimerTask() {
				@Override
				public void run(Timeout timeout) {
					tryReconnect(Math.min(BACKOFF_CAP, attempts + 1));
				}
			}, timeout, TimeUnit.MILLISECONDS);
		} catch (IllegalStateException e) {
			// skip
		}
	}

	private void tryReconnect(final int nextAttempt) {
		if (bootstrap.config().group().isShuttingDown()) {
			return;
		}

		if (addressLoader != null) {
			bootstrap.remoteAddress(addressLoader.getRemoveOldAddress());
		}

		log.info("Reconnecting to {}", bootstrap.config().remoteAddress());
		bootstrap.connect().addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (bootstrap.config().group().isShuttingDown()) {
					return;
				}
				if (!future.isSuccess()) {
					log.warn("Reconnect to {} failed: {}", bootstrap.config().remoteAddress(), future.cause().getMessage());
					reconnect(nextAttempt);
					return;
				}
				log.info("Reconnect to {} successfully", bootstrap.config().remoteAddress());

				for (ReconnectListener listener : listeners) {
					try {
						listener.reconnected(future.channel());
					} catch (Throwable e) {
						log.error("An exception was thrown by {}.reconnected()", listener.getClass().getName(), e);
					}
				}
			}
		});
	}

	public interface ReconnectListener {

		void reconnected(Channel channel);
	}

}
