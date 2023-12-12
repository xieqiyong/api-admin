/*
 * Created by zhangzxiang91@gmail.com on 2021/06/01.
 */
package com.hz.api.admin.netkit.client;

import io.netty.channel.Channel;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/01.
 */
public interface ConnectionListener {

	void channelConnected(Channel channel);

	void channelClosed(Channel channel);

	void exceptionCaught(Channel channel, Throwable cause);

	void channelReconnected(Channel channel);

}
