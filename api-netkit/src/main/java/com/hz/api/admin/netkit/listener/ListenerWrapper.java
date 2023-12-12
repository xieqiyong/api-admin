/*
 * Created by zhangzxiang91@gmail.com on 2021/06/07.
 */
package com.hz.api.admin.netkit.listener;


import com.hz.api.admin.netkit.filter.PacketFilter;
import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/07.
 */
public class ListenerWrapper {

	private final PacketListener<?> listener;
	private final PacketFilter filter;

	public ListenerWrapper(PacketListener<?> listener, PacketFilter filter) {
		this.listener = listener;
		this.filter = filter;
	}

	public boolean filterMatches(Packet packet) {
		return filter == null || filter.accept(packet);
	}

	public PacketListener<?> getListener() {
		return listener;
	}

	public PacketFilter getFilter() {
		return filter;
	}
}
