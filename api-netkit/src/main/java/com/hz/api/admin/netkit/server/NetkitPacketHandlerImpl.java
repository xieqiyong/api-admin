package com.hz.api.admin.netkit.server;

import com.hz.api.admin.netkit.NetkitPacketHandler;
import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.packet.PacketError;
import com.hz.api.admin.netkit.packet.PingPacket;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/04.
 */
@Slf4j
public class NetkitPacketHandlerImpl implements NetkitPacketHandler {

	private final NetkitConnection                             connection;
	private final Map<String, PacketHandler<? extends Packet>> handlers;

	public NetkitPacketHandlerImpl(NetkitServer server, NetkitConnection connection) {
		this.connection = connection;
		this.handlers = server.getPacketHandlers();
	}

	/**
	 * 消息接收处理，TODO 线程池优化
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void processPacket(final Packet packet) {
		if (packet.getType() == Packet.Type.request) {
			if (packet instanceof PingPacket) {
				return;
			}

			PacketHandler handler = handlers.get(packet.getName());
			try {
				Packet reply = handler.handle(connection, packet);
				if (reply != null) {
					connection.sendPacket(reply);
				}
			} catch (Throwable e) {
				log.error("An exception was thrown by {}.handle error: channel=[id:{} addr:{}], packet={}", handler.getClass().getName(),
						connection.getChannel().id(), connection.getChannel().remoteAddress(), packet, e);
				try {
					Packet reply;
					if (e instanceof PacketError) {
						reply = Packet.createError(packet, (PacketError)e);
					} else {
						reply = Packet.createError(packet, PacketError.buildServerError(e.getMessage()));
					}
					connection.sendPacket(reply);
				} catch (Throwable ignored) {
				}
			}
		} else {
			connection.processPacket(packet);
		}
	}

}
