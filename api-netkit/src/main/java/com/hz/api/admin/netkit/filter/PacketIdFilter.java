/*
 * Created by zhangzxiang91@gmail.com on 2021/05/08.
 */
package com.hz.api.admin.netkit.filter;

import com.hz.api.admin.netkit.packet.Packet;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/08.
 */
public class PacketIdFilter implements PacketFilter {

	private final String packetId;

	public PacketIdFilter(String packetId) {
		if (StringUtils.isBlank(packetId)) {
			throw new IllegalArgumentException("Packet ID must not be null nor empty.");
		}
		this.packetId = packetId;
	}

	@Override
	public boolean accept(Packet packet) {
		if (packet == null) {
			return false;
		}
		return packetId.equals(packet.getId());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": packetId=" + packetId;
	}
}
