package com.hz.api.admin.engine.transport.handler;

import com.perfma.xsea.data.collector.netkit.packet.Packet;
import com.perfma.xsea.data.collector.netkit.server.NetkitServer;
import com.perfma.xsea.data.collector.netkit.server.PacketHandler;

/**
 *
 * @author liusu
 */
public abstract class AbstractPacketHandler<T extends Packet> implements PacketHandler<T> {

	@Override
	public void init(NetkitServer server) {

	}
}
