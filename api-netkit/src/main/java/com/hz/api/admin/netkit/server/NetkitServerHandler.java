package com.hz.api.admin.netkit.server;

import com.hz.api.admin.netkit.NetkitPacketHandler;
import com.hz.api.admin.netkit.packet.Packet;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/07.
 */
@Slf4j
public class NetkitServerHandler extends SimpleChannelInboundHandler<Packet> {

	private static final AttributeKey<NetkitConnection>    NETKIT_CONNECTION_KEY = AttributeKey.valueOf("NETKIT_CONNECTION");
	private static final AttributeKey<NetkitPacketHandler> NETKIT_HANDLER_KEY    = AttributeKey.valueOf("NETKIT_HANDLER");

	private final ChannelGroup channels;
	private final NetkitServer server;

	public NetkitServerHandler(NetkitServer server, ChannelGroup channels) {
		this.channels = channels;
		this.server = server;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.channels.add(ctx.channel());
		NetkitConnection connection = server.getConnectionManager().createConnection(ctx.channel());
		NetkitPacketHandler handler = new NetkitPacketHandlerImpl(server, connection);
		ctx.channel().attr(NETKIT_CONNECTION_KEY).set(connection);
		ctx.channel().attr(NETKIT_HANDLER_KEY).set(handler);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.channels.remove(ctx.channel());
		log.debug("Closing connection due to inactive in channel: {}", ctx.channel());
		closeConnection(ctx.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.warn("Closing connection due to exception in channel: {}", ctx.channel(), cause);
		closeConnection(ctx.channel());
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
		NetkitPacketHandler handler = ctx.channel().attr(NETKIT_HANDLER_KEY).get();
		NetkitConnection connection = ctx.channel().attr(NETKIT_CONNECTION_KEY).get();
		// 首次接收消息，绑定通信协议
		if (connection.getProtocolHeader() == null) {
			synchronized (connection) {
				if (connection.getProtocolHeader() == null) {
					connection.bindProtocolHeader(msg.getHeader(), false);
				}
			}
		}
		try {
			handler.processPacket(msg);
		} catch (Throwable e) {
			log.error("Closing connection due to error while processing message: {}", msg, e);
			closeConnection(ctx.channel());
		}
	}

	private void closeConnection(Channel channel) {
		NetkitConnection connection = channel.attr(NETKIT_CONNECTION_KEY).get();
		if (connection != null) {
			connection.close();
		}
	}
}
