/*
 * Created by zhangzxiang91@gmail.com on 2021/07/26.
 */
package com.hz.api.admin.netkit.client;

import io.netty.channel.Channel;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/07/26.
 */
public class ConnectionListenerAdapter implements ConnectionListener{

	@Override
	public void channelConnected(Channel channel) {

	}

	@Override
	public void channelClosed(Channel channel) {

	}

	@Override
	public void exceptionCaught(Channel channel, Throwable cause) {

	}

	@Override
	public void channelReconnected(Channel channel) {

	}
}
