/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.client;

import java.net.InetSocketAddress;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public interface AddressLoader {

	InetSocketAddress getAddress();

	InetSocketAddress getRemoveOldAddress();
}
