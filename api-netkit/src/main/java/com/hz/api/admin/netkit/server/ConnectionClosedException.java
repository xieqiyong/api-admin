package com.hz.api.admin.netkit.server;


import com.hz.api.admin.netkit.filter.PacketFilter;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/27.
 */
public class ConnectionClosedException extends Exception {

	public ConnectionClosedException(NetkitConnection connection) {
	}

	public ConnectionClosedException(NetkitConnection connection, PacketFilter replyFilter) {

	}
}
