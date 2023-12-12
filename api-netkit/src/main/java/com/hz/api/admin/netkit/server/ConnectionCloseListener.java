package com.hz.api.admin.netkit.server;

public interface ConnectionCloseListener<T> {

	void onConnectionClose(T handback);
}
