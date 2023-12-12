/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.serialize;


import com.hz.api.admin.netkit.packet.Packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public interface Serialization {

	String getName();

	byte[] serialize(Packet packet) throws SerializationException;

	Packet deserialize(byte[] body) throws SerializationException;

}
