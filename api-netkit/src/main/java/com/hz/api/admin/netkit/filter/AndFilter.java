/*
 * Created by zhangzxiang91@gmail.com on 2021/05/08.
 */
package com.hz.api.admin.netkit.filter;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/08.
 */
public class AndFilter extends AbstractListFilter implements PacketFilter {

	public AndFilter() {
		super();
	}

	public AndFilter(PacketFilter... filters) {
		super(filters);
	}

	@Override
	public boolean accept(Packet packet) {
		for (PacketFilter filter : filters) {
			if (!filter.accept(packet)) {
				return false;
			}
		}
		return true;
	}

}
