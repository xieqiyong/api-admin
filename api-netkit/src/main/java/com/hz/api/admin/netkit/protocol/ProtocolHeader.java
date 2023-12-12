package com.hz.api.admin.netkit.protocol;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;


/**
 * 协议头数据
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class ProtocolHeader {

	/**
	 * 协议名称
	 */
	private String              protocol;
	/**
	 * 序列化名称
	 */
	private String              serialization;
	/**
	 * 是否加密
	 */
	private boolean             encrypt;
	/**
	 * 扩展协议头
	 */
	private Map<String, String> extend;

}
