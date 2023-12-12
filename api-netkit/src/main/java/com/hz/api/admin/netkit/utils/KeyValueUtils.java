package com.hz.api.admin.netkit.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.Charsets;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class KeyValueUtils {

	/**
	 * 解析byte数据，将其转化为key和value数据
	 *
	 * @param data 按照key和value的格式序列化的字节数组
	 * @return 转化为map
	 */
	public static Map<String, String> mapDeserialize(byte[] data) {
		if (data == null || data.length <= 0) {
			return null;
		}

		int keySize, valueSize;
		byte[] keyContent, valueContent;
		ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
		try {
			Map<String, String> map = new HashMap<String, String>();
			while (byteBuf.readableBytes() > 0) {
				keySize = byteBuf.readShort();
				keyContent = new byte[keySize];
				byteBuf.readBytes(keyContent);

				valueSize = byteBuf.readInt();
				valueContent = new byte[valueSize];
				byteBuf.readBytes(valueContent);

				map.put(new String(keyContent, Charsets.UTF_8), new String(valueContent, Charsets.UTF_8));
			}
			return map;
		} finally {
			byteBuf.release();
		}
	}

	public static byte[] mapSerialize(Map<String, String> map) {
		if (MapUtils.isEmpty(map)) {
			return new byte[0];
		}

		ByteBuf byteBuf = Unpooled.buffer();
		try {
			byte[] keyContent, valueContent;
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (StringUtils.isEmpty(entry.getKey()) || StringUtils.isEmpty(entry.getValue())) {
					continue;
				}

				keyContent = entry.getKey().getBytes(Charsets.UTF_8);
				valueContent = entry.getValue().getBytes(Charsets.UTF_8);
				byteBuf.writeShort((short)keyContent.length);
				byteBuf.writeBytes(keyContent);
				byteBuf.writeInt(valueContent.length);
				byteBuf.writeBytes(valueContent);
			}

			byte[] bytes = new byte[byteBuf.readableBytes()];
			byteBuf.readBytes(bytes);
			return bytes;
		} finally {
			byteBuf.release();
		}
	}
}
