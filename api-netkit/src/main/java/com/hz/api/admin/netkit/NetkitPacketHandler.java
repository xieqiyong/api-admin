package com.hz.api.admin.netkit;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/06.
 */
public interface NetkitPacketHandler {

	void processPacket(final Packet packet);
}
