/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.serialize;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public class SerializationException extends RuntimeException {

	public SerializationException() {
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

}
