/*
 * Created by zhangzxiang91@gmail.com on 2021/05/06.
 */
package com.hz.api.admin.netkit.packet;

import com.google.gson.annotations.Expose;
import com.hz.api.admin.netkit.filter.PacketReplyFilter;
import com.hz.api.admin.netkit.protocol.ProtocolHeader;
import com.hz.api.admin.netkit.utils.GsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/06.
 */
@Getter
@Setter
public abstract class Packet implements Serializable {

	@Expose(serialize = false, deserialize = false)
	protected transient ProtocolHeader header;

	private String      id;
	private Type        type;
	private Long        bornTime;
	private PacketError error;

	public Packet() {
		this.id = getNewPacketId();
		this.type = Type.request;
	}

	public static String getNewPacketId() {
		return RandomStringUtils.randomAlphanumeric(8) + "-" + RandomStringUtils.randomNumeric(3);
	}

	public static <T extends Packet> T createResult(T packet) {
		if (packet.getType() != Type.request) {
			throw new IllegalArgumentException("Packet must be of type 'request'. Packet: " + packet);
		}

		try {
			Constructor<? extends Packet> constructor = packet.getClass().getConstructor();
			//noinspection unchecked
			T result = (T)constructor.newInstance();
			result.setId(packet.getId());
			result.setType(Type.result);
			return result;
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T extends Packet> T createError(T packet, PacketError packetError) {
		T reply = createResult(packet);
		reply.setType(Type.error);
		reply.setError(packetError);
		return reply;
	}

	public PacketReplyFilter createReplyFilter() {
		return new PacketReplyFilter(this);
	}

	public abstract String getName();

	public void setHeader(ProtocolHeader header) {
		this.header = Objects.requireNonNull(header, "'header' must not be null");
	}

	public void setId(String id) {
		this.id = Objects.requireNonNull(StringUtils.trimToNull(id), "'id' must not be null");
	}

	public void setType(Type type) {
		this.type = Objects.requireNonNull(type, "'type' must not be null");
	}

	public void setBornTime(Long bornTime) {
		this.bornTime = Objects.requireNonNull(bornTime, "'bornTime' must not be null");
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + GsonUtils.toString(this);
	}

	/**
	 * TODO 兼容早期JSON方式通信协议，等所有xcenter-client升级到v2.0之后就可以删除了
	 */
	@Deprecated
	public Map<String, Object> getExtensionsData() {
		return new HashMap<String, Object>();
	}

	public boolean isRequest() {
		return type == Type.request;
	}

	public boolean isResponse() {
		return !isRequest();
	}

	public boolean isError() {
		return type == Type.error;
	}

	public void setError(PacketError error) {
		this.error = error;
		if (error != null) {
			setType(Type.error);
		}
	}

	public enum Type {

		request,

		result,

		error,
		;

		public static Type fromString(String value) {
			for (Type type : values()) {
				if (type.name().equals(value)) {
					return type;
				}
			}
			return null;
		}
	}
}
