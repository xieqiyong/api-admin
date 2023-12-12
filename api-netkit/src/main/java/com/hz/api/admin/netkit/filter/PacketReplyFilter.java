/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.filter;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class PacketReplyFilter implements PacketFilter {

	private final PacketFilter andIdFilter;

	public PacketReplyFilter(Packet packet) {
		if (!packet.isRequest()) {
			throw new IllegalArgumentException("Packet must be a request packet.");
		}

		PacketFilter typeFilter = new OrFilter(PacketTypeFilter.ERROR, PacketTypeFilter.RESULT);
		PacketFilter idFilter = new PacketIdFilter(packet.getId());
		andIdFilter = new AndFilter(typeFilter, idFilter);
	}

	@Override
	public boolean accept(Packet packet) {
		return andIdFilter.accept(packet);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ": andIdFilter (" + andIdFilter.toString() + ")";
	}
}
