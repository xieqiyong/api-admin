/*
 * Created by zhangzxiang91@gmail.com on 2022/04/27.
 */
package com.hz.api.admin.netkit.protocol;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/27.
 */
public class PacketException extends Exception {

	public PacketException() {
	}

	public PacketException(String message) {
		super(message);
	}

	public PacketException(String message, Throwable cause) {
		super(message, cause);
	}

	public PacketException(Throwable cause) {
		super(cause);
	}
}
