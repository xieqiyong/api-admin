package com.hz.api.admin.netkit.filter;


import com.hz.api.admin.netkit.packet.Packet;

import java.util.Objects;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class PacketTypeFilter implements PacketFilter {

	public static final PacketFilter REQUEST = new PacketTypeFilter(Packet.Type.request);
	public static final PacketFilter RESULT  = new PacketTypeFilter(Packet.Type.result);
	public static final PacketFilter ERROR   = new PacketTypeFilter(Packet.Type.error);

	private final Packet.Type type;

	private PacketTypeFilter(Packet.Type type) {
		this.type = Objects.requireNonNull(type, "Type must not be null");
	}

	@Override
	public boolean accept(Packet packet) {
		return packet.getType() == type;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": type=" + type;
	}
}

