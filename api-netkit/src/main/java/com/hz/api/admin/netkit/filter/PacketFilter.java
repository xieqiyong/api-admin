package com.hz.api.admin.netkit.filter;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/08.
 */
public interface PacketFilter {

	boolean accept(Packet packet);

}
