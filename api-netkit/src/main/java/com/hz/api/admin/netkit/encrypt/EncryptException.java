/*
 * Created by zhangzxiang91@gmail.com on 2022/04/27.
 */
package com.hz.api.admin.netkit.encrypt;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2022/04/27.
 */
public class EncryptException extends RuntimeException {

	public EncryptException() {
	}

	public EncryptException(String message) {
		super(message);
	}

	public EncryptException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncryptException(Throwable cause) {
		super(cause);
	}
}
