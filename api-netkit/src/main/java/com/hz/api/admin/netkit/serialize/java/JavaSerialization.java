/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.serialize.java;


import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.serialize.Serialization;
import com.hz.api.admin.netkit.serialize.SerializationException;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 基于Java的对象序列化
 *
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public class JavaSerialization implements Serialization {

	public static final String SERIALIZATION_NAME = "java";

	@Override
	public String getName() {
		return SERIALIZATION_NAME;
	}

	@Override
	public byte[] serialize(Packet packet) throws SerializationException {
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(packet);
			return baos.toByteArray();
		} catch (Throwable e) {
			throw new SerializationException(e);
		} finally {
			IOUtils.closeQuietly(oos);
		}
	}

	@Override
	public Packet deserialize(byte[] body) throws SerializationException {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(body));
			return (Packet)ois.readObject();
		} catch (Throwable e) {
			throw new SerializationException(e);
		} finally {
			IOUtils.closeQuietly(ois);
		}
	}
}
