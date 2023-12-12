/*
 * Created by zhangzxiang91@gmail.com on 2022/04/26.
 */
package com.hz.api.admin.netkit.protocol;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/26.
 */
public class ProtocolException extends Exception {

	public ProtocolException() {
		super();
	}

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProtocolException(Throwable cause) {
		super(cause);
	}

}
