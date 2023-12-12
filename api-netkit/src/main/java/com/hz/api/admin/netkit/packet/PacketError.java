/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.packet;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class PacketError extends RuntimeException {

	public static final PacketError INTERNAL_SERVER_ERROR = new PacketError("INTERNAL_SERVER_ERROR", "Internal server error");
	public static final PacketError INTERNAL_CLIENT_ERROR = new PacketError("INTERNAL_CLIENT_ERROR", "Internal client error");

	private final String code;
	private final String message;

	public PacketError(String code, String message) {
		super(code);
		this.code = code;
		this.message = message;
	}

	public static PacketError buildError(String code, String message) {
		return new PacketError(code, message);
	}

	public static PacketError buildServerError(String message) {
		return buildError(INTERNAL_SERVER_ERROR.getCode(), message);
	}

	public static PacketError buildClientError(String message) {
		return buildError(INTERNAL_CLIENT_ERROR.getCode(), message);
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "PacketError{" + "code='" + code + '\'' + ", message='" + message + '\'' + '}';
	}
}
