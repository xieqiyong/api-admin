/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.protocol;

import com.hz.api.admin.netkit.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public interface Protocol {

	String getName();

	boolean supports(ByteBuf buf);

	byte[] encode(Channel channel, Packet packet) throws PacketException;

	Packet decode(Channel channel, ByteBuf byteBuf) throws PacketException;

}
