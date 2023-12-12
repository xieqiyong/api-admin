/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.serialize.fst;


import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.serialize.Serialization;
import com.hz.api.admin.netkit.serialize.SerializationException;
import org.nustaq.serialization.FSTConfiguration;

/**
 * 基于FST框架的对象序列化
 *
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public class FstSerialization implements Serialization {

	public static final String SERIALIZATION_NAME = "fst";

	private final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

	@Override
	public String getName() {
		return SERIALIZATION_NAME;
	}

	@Override
	public byte[] serialize(Packet packet) throws SerializationException {
		return conf.asByteArray(packet);
	}

	@Override
	public Packet deserialize(byte[] body) throws SerializationException {
		return (Packet)conf.asObject(body);
	}
}
