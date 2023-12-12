/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.protocol;

import com.hz.api.admin.netkit.encrypt.EncryptManager;
import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.serialize.SerializationManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public abstract class AbstractProtocol implements Protocol {

	@Override
	public byte[] encode(Channel channel, Packet packet) throws PacketException {
		ProtocolHeader header = packet.getHeader();
		byte[] dataBytes = SerializationManager.serialize(header.getSerialization(), packet);

		// 加密处理
		if (header.isEncrypt()) {
			dataBytes = EncryptManager.encrypt(channel.id().asLongText(), dataBytes);
		}

		return dataBytes;
	}

	@Override
	public Packet decode(Channel channel, ByteBuf byteBuf) throws PacketException {
		// 读取协议头
		ProtocolHeader header = decodeHeader(byteBuf);
		byte[] dataBytes = ByteBufUtil.getBytes(byteBuf);

		// 解密处理
		if (header.isEncrypt()) {
			dataBytes = EncryptManager.decrypt(channel.id().asLongText(), dataBytes);
		}

		Packet packet = SerializationManager.deserialize(header.getSerialization(), dataBytes);
		// 设置协议头
		packet.setHeader(header);
		return packet;
	}

	protected abstract ProtocolHeader decodeHeader(ByteBuf byteBuf) throws PacketException;

}
