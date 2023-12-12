package com.hz.api.admin.netkit.filter;

import com.perfma.xsea.data.collector.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/03.
 */
public class OrFilter extends AbstractListFilter implements PacketFilter {

	public OrFilter() {
		super();
	}

	public OrFilter(PacketFilter... filters) {
		super(filters);
	}

	@Override
	public boolean accept(Packet packet) {
		for (PacketFilter filter : filters) {
			if (filter.accept(packet)) {
				return true;
			}
		}
		return false;
	}

}
