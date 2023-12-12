/*
 * Created by zhangzxiang91@gmail.com on 2021/06/24.
 */
package com.hz.api.admin.netkit.server;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/24.
 */
public interface ConnectionListener {

	/**
	 * 连接创建通知
	 */
	void created(NetkitConnection connection);

	/**
	 * 连接移除通知
	 */
	void removed(NetkitConnection connection);

}
