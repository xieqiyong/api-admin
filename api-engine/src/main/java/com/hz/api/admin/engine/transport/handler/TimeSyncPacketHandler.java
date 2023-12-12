
package com.hz.api.admin.engine.transport.handler;

import com.perfma.xsea.data.collector.base.packet.TimeSyncPacket;
import com.perfma.xsea.data.collector.exception.DataServerException;
import com.perfma.xsea.data.collector.model.ClientInfo;
import com.perfma.xsea.data.collector.netkit.packet.Packet;
import com.perfma.xsea.data.collector.netkit.server.NetkitConnection;
import com.perfma.xsea.data.collector.transport.ClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author liusu
 */
@Component
public class TimeSyncPacketHandler extends AbstractPacketHandler<TimeSyncPacket> {

	private static final Logger log = LoggerFactory.getLogger(TimeSyncPacketHandler.class);

	@Resource
	private ClientConnectionManager clientConnectionManager;


	@Override
	public String getName() {
		return TimeSyncPacket.NAME;
	}

	@Override
	public TimeSyncPacket handle(NetkitConnection connection, TimeSyncPacket packet) {
		ClientInfo clientInfo = clientConnectionManager.getClientInfoByChannelId(connection.getChannelId());
		if (clientInfo == null) {
			throw new DataServerException("客户端未注册");
		}

		long diff = System.currentTimeMillis() - packet.getTimestamp();
		TimeSyncPacket reply = Packet.createResult(packet);
		reply.setResult(diff);
		return reply;
	}
}
