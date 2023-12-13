
package com.hz.api.admin.model.enums;

/**
 * 客户端状态
 *
 * @author zhangzxiang91@gmail.com
 * @date 2021/07/06.
 */
public enum ClientState {
	/**
	 * 与服务端建立连接成功
	 */
	CONNECTED,
	/**
	 * 注册到服务端成功
	 */
	REGISTERED,
	/**
	 * 与服务端连接已断开
	 */
	DISCONNECT
}
