/*
 * Created by zhangzxiang91@gmail.com on 2021/06/02.
 */
package com.hz.api.admin.netkit.serialize;

import com.hz.api.admin.netkit.packet.Packet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/02.
 */
@Slf4j
public class SerializationManager {

	private static final Map<String, Serialization> SERIALIZATION_MAP = new HashMap<String, Serialization>();
	private static final AtomicBoolean              inited            = new AtomicBoolean();

	static {
		init();
	}

	public static void init() {
		if (!inited.compareAndSet(false, true)) {
			return;
		}

		final ServiceLoader<Serialization> serializations = ServiceLoader.load(Serialization.class, SerializationManager.class.getClassLoader());
		for (Serialization serialization : serializations) {
			addSerialization(serialization);
		}
	}

	public static void addSerialization(Serialization serialization) {
		if (StringUtils.isBlank(serialization.getName())) {
			throw new IllegalArgumentException("Serialization '" + serialization.getClass().getName() + "' name must not be null");
		}
		if (SERIALIZATION_MAP.containsKey(serialization.getName())) {
			throw new IllegalArgumentException(
					"Serialization '" + serialization.getClass().getName() + ":" + serialization.getName() + "' already exists");
		}
		SERIALIZATION_MAP.put(serialization.getName(), serialization);
		log.info("Add Serialization[{} {}]", serialization.getName(), serialization.getClass().getName());
	}

	private static Serialization getSerialization(String name) {
		Serialization serialization = SERIALIZATION_MAP.get(name);
		if (serialization == null) {
			throw new SerializationException("Serialization [" + name + "] not found.");
		}
		return serialization;
	}

	public static byte[] serialize(String serializationName, Packet packet) {
		Serialization serialization = getSerialization(serializationName);
		return serialization.serialize(packet);
	}

	public static Packet deserialize(String serializationName, byte[] body) {
		Serialization serialization = getSerialization(serializationName);
		return serialization.deserialize(body);
	}
}
