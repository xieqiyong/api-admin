/*
 * Created by zhangzxiang91@gmail.com on 2020/11/04.
 */
package com.hz.api.admin.netkit.client;

import com.hz.api.admin.netkit.packet.PingPacket;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理心跳数据包发送
 *
 * @author zhangzxiang91@gmail.com
 * @date 2020/11/04.
 */
@ChannelHandler.Sharable
@Slf4j
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter {

	private final NetkitClient client;

	public ConnectorIdleStateTrigger(NetkitClient client) {
		this.client = client;
	}

	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, Object evt) throws Exception {
		if (client.isConnected() && evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent)evt).state();
			if (state == IdleState.WRITER_IDLE) {
				client.sendPacket(new PingPacket()).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) {
						if (!future.isSuccess()) {
							log.warn("failed to send heartbeat to server '{}', error=\"{}\"", ctx.channel().remoteAddress(),
									future.cause().getMessage());
						}
					}
				});
			}
		}
		super.userEventTriggered(ctx, evt);
	}
}
