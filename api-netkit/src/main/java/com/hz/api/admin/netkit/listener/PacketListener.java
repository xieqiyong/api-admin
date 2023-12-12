/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.listener;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public interface PacketListener<T extends Packet> {

	void processPacket(T packet);
}
