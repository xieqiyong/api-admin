/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.protocol.netkit;


import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.protocol.AbstractProtocol;
import com.hz.api.admin.netkit.protocol.PacketException;
import com.hz.api.admin.netkit.protocol.ProtocolHeader;
import com.hz.api.admin.netkit.utils.KeyValueUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * ----------------------------------------------------------
 * | 第1字节  | 第2~5字节 | 第6字节   | 第7~N字节   | 剩余字节   |
 * ----------------------------------------------------------
 * | 协议标识 | 数据长度   | 协议头长度 | 扩展协议头  | 数据内容  |
 * -----------------------------------------------------------
 *
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public class NetkitProtocol extends AbstractProtocol {

	// 协议名称
	public static final String PROTOCOL_NAME = "netkit";

	// 全新设计的自定义通信协议，数据内容第一个字节为: 10
	private static final byte[] PROTOCOL_MARK = {(byte)10};

	// 协议头长度
	private static final int HEAD_LENGTH     = 6;
	// 协议头最大长度
	private static final int HEAD_MAX_LENGTH = Short.MAX_VALUE - HEAD_LENGTH;

	// ------------------------- 内置协议头 -------------------------
	// 序列化方式
	private static final String        HEADER_SERIALIZATION_KEY = "$netkit.serialization";
	// 加密标识
	private static final String        HEADER_ENCRYPT_KEY       = "$netkit.encrypt";

	@Override
	public String getName() {
		return PROTOCOL_NAME;
	}

	@Override
	public boolean supports(ByteBuf buf) {
		byte[] mark = new byte[PROTOCOL_MARK.length];
		buf.getBytes(0, mark);
		return Arrays.equals(PROTOCOL_MARK, mark);
	}

	@Override
	public byte[] encode(Channel channel, Packet packet) throws PacketException {
		byte[] dataBytes = super.encode(channel, packet);
		ProtocolHeader header = packet.getHeader();

		// 扩展协议头
		Map<String, String> extendMap = new HashMap<String, String>();
		if (header.getExtend() != null) {
			extendMap.putAll(header.getExtend());
		}
		// 设置内置协议头
		extendMap.put(HEADER_SERIALIZATION_KEY, header.getSerialization());
		extendMap.put(HEADER_ENCRYPT_KEY, String.valueOf(header.isEncrypt()));
		// 序列化扩展协议头为字节数组
		byte[] extendBytes = KeyValueUtils.mapSerialize(extendMap);
		if (extendBytes.length > HEAD_MAX_LENGTH) {
			throw new PacketException("数据包协议头长度 " + extendBytes.length + "超过最大 " + HEAD_MAX_LENGTH + " 限制");
		}

		// 协议头长度
		short headerLength = (short)(HEAD_LENGTH + extendBytes.length);
		// 数据内容长度
		int dataLength = headerLength + dataBytes.length;

		// 协议头
		ByteBuffer headerBuffer = ByteBuffer.allocate(headerLength + PROTOCOL_MARK.length);
		// 写入协议标识
		headerBuffer.put(PROTOCOL_MARK);
		// 写入数据内容长度
		headerBuffer.putInt(dataLength);
		// 写入协议头长度
		headerBuffer.putShort(headerLength);
		// 写入扩展协议头
		headerBuffer.put(extendBytes);

		return ArrayUtils.addAll(headerBuffer.array(), dataBytes);
	}

	@Override
	protected ProtocolHeader decodeHeader(ByteBuf byteBuf) throws PacketException {
		//移除第一个协议标识字节
		byteBuf.readBytes(new byte[PROTOCOL_MARK.length]);

		// 读取数据内容长度
		int dataLength = byteBuf.readInt();
		if (byteBuf.readableBytes() != dataLength - 4) {
			throw new PacketException("数据包协议格式错误: dataLength=" + dataLength + ", readableLength=" + byteBuf.readableBytes());
		}

		// 读取协议头长度
		short headerLength = byteBuf.readShort();

		// 读取扩展协议头
		byte[] extendBytes = new byte[headerLength - HEAD_LENGTH];
		byteBuf.readBytes(extendBytes);
		Map<String, String> extendMap = KeyValueUtils.mapDeserialize(extendBytes);

		ProtocolHeader header = new ProtocolHeader();
		header.setProtocol(getName());
		header.setSerialization(extendMap.remove(HEADER_SERIALIZATION_KEY));
		header.setEncrypt(Boolean.parseBoolean(extendMap.remove(HEADER_ENCRYPT_KEY)));
		header.setExtend(extendMap);
		return header;
	}

}
