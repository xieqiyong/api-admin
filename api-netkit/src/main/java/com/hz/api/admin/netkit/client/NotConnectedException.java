/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.client;


import com.hz.api.admin.netkit.filter.PacketFilter;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public class NotConnectedException extends Exception {

	public NotConnectedException() {
		this(null);
	}

	public NotConnectedException(String optionalHint) {
		super("Client is not, or no longer, connected." + (optionalHint != null ? ' ' + optionalHint : ""));
	}

	public NotConnectedException(NetkitClient connection, String details) {
		super("The connection " + connection.toString() + " is no longer connected. " + details);
	}

	public NotConnectedException(NetkitClient connection, PacketFilter stanzaFilter) {
		super("The connection " + connection + " is no longer connected while waiting for response with " + stanzaFilter);
	}

	public NotConnectedException(NetkitClient connection, PacketFilter stanzaFilter, Exception connectionException) {
		super("The connection " + connection + " is no longer connected while waiting for response with " + stanzaFilter + " because of "
				+ connectionException, connectionException);
	}
}
