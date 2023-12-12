/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.filter;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class PacketClassFilter implements PacketFilter {

	private final Class<? extends Packet> packetType;

	public PacketClassFilter(Class<? extends Packet> packetType) {
		if (!Packet.class.isAssignableFrom(packetType)) {
			throw new IllegalArgumentException("Packet class must be a sub-class of Packet.");
		} else {
			this.packetType = packetType;
		}
	}

	public boolean accept(Packet packet) {
		return this.packetType.isInstance(packet);
	}

	public String toString() {
		return getClass().getSimpleName() + ": packetType=" + packetType.getName();
	}
}

