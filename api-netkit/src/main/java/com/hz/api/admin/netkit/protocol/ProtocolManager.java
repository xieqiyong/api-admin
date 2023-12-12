/*
 * Created by zhangzxiang91@gmail.com on 2021/06/02.
 */
package com.hz.api.admin.netkit.protocol;

import com.hz.api.admin.netkit.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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
public class ProtocolManager {

	private static final Map<String, Protocol> PROTOCOL_MAP = new HashMap<String, Protocol>();
	private static final AtomicBoolean         inited       = new AtomicBoolean();

	static {
		init();
	}

	public static void init() {
		if (!inited.compareAndSet(false, true)) {
			return;
		}

		final ServiceLoader<Protocol> protocols = ServiceLoader.load(Protocol.class, ProtocolManager.class.getClassLoader());
		for (Protocol protocol : protocols) {
			addProtocol(protocol);
		}
	}

	public static void addProtocol(Protocol protocol) {
		if (StringUtils.isBlank(protocol.getName())) {
			throw new IllegalArgumentException("Protocol '" + protocol.getClass().getName() + "' name must not be null");
		}
		if (PROTOCOL_MAP.containsKey(protocol.getName())) {
			throw new IllegalArgumentException("Protocol '" + protocol.getClass().getName() + ":" + protocol.getName() + "' already exists");
		}
		PROTOCOL_MAP.put(protocol.getName(), protocol);
		log.info("Add Protocol[{} {}]", protocol.getName(), protocol.getClass().getName());
	}

	public static Protocol getProtocol(String name) {
		return PROTOCOL_MAP.get(name);
	}

	public static byte[] encode(Channel channel, Packet packet) throws ProtocolException, PacketException {
		ProtocolHeader header = packet.getHeader();
		Protocol protocol = PROTOCOL_MAP.get(header.getProtocol());
		if (protocol == null) {
			throw new ProtocolException("Channel:'" + channel.remoteAddress() + "' protocol [" + header.getProtocol() + "] not found.");
		}
		return protocol.encode(channel, packet);
	}

	public static Packet decode(Channel channel, ByteBuf byteBuf) throws ProtocolException, PacketException {
		// 查找支持的协议
		Protocol protocol = null;
		for (Protocol p : PROTOCOL_MAP.values()) {
			if (p.supports(byteBuf)) {
				protocol = p;
			}
		}
		if (protocol == null) {
			throw new ProtocolException("Channel:'" + channel.remoteAddress() + "' no matching protocol.");
		}

		return protocol.decode(channel, byteBuf);
	}

}
