package com.hz.api.admin.engine.transport.handler;


import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.server.NetkitServer;
import com.hz.api.admin.netkit.server.PacketHandler;

/**
 *
 * @author liusu
 */
public abstract class AbstractPacketHandler<T extends Packet> implements PacketHandler<T> {

	@Override
	public void init(NetkitServer server) {

	}
}
