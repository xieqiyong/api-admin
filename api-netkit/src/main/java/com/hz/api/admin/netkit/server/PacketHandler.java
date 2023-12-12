package com.hz.api.admin.netkit.server;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/06.
 */
public interface PacketHandler<T extends Packet> {

	String getName();

	void init(NetkitServer server);

	T handle(NetkitConnection connection, T packet);

}
