/*
 * Created by zhangzxiang91@gmail.com on 2021/06/03.
 */
package com.hz.api.admin.netkit.packet;

import java.util.Map;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/03.
 */
public class ServerErrorPacket extends Packet {

	public static final String NAME = "system:server-error";

	public ServerErrorPacket() {
		setType(Type.request);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public Map<String, Object> getExtensionsData() {
		return null;
	}
}
