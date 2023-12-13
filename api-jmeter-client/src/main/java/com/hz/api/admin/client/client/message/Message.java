package com.hz.api.admin.client.client.message;

import com.hz.api.admin.netkit.utils.GsonUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.Charsets;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/10.
 */
@NoArgsConstructor
@Data
public class Message {

	/**
	 * 消息topic
	 */
	private String topic;

	/**
	 * 消息内容
	 */
	private byte[] body;

	/**
	 * 是否应答
	 */
	private boolean acks;

	/**
	 * 是否是字节数组
	 */
	private boolean isByteArray=false;

	public Message(String topic, byte[] body) {
		this.topic = topic;
		this.body = body;
		this.isByteArray = true;
	}

	/**
	 * TODO 兼容低版本传入Object类型消息内容自动转json的场景，请使用{@link Message#Message(String, byte[])}
	 */
	public Message(String topic, Object body) {
		this.topic = topic;
		if (body instanceof String) {
			this.body = ((String)body).getBytes(Charsets.UTF_8);
		} else if (body instanceof byte[]) {
			this.body = (byte[])body;
			this.isByteArray = true;
		} else {
			this.body = GsonUtils.toBytes(body);
		}
	}

	@Deprecated
	public void setBody(Object body) {
		if (body instanceof String) {
			this.body = ((String)body).getBytes(Charsets.UTF_8);
		} else if (body instanceof byte[]) {
			this.body = (byte[])body;
			this.isByteArray = true;
		} else {
			this.body = GsonUtils.toBytes(body);
		}
	}

	@Override
	public String toString() {
		return "Message{" + "topic='" + topic + '\'' + ", body='" + new String(body, Charsets.UTF_8) + '\'' + '}';
	}
}
