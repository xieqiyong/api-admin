/*
 * Created by zhangzxiang91@gmail.com on 2021/06/03.
 */
package com.hz.api.admin.netkit.listener;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/03.
 */
public interface ReplyListener<T extends Packet> {

	void onPacket(T packet);

	void onFailure(Throwable e);
}
