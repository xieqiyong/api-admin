package com.hz.api.admin.netkit.codec;

import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.protocol.ProtocolManager;
import com.hz.api.admin.netkit.utils.GsonUtils;
import com.hz.api.admin.netkit.utils.SystemUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/07.
 */
@Slf4j
public class PacketEncoder extends MessageToByteEncoder<Packet> {

	private static final byte[] LENGTH_PLACEHOLDER = new byte[4];

	private static final boolean debug = Boolean.parseBoolean(SystemUtils.getProp("netkit.encoder.debug", "false"));
	private static final int SEND_MESSAGE_MAX_SIZE = Integer.parseInt(SystemUtils.getProp("netkit.message.max.size", "31457280"));


	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
		if (StringUtils.isBlank(msg.getId())) {
			msg.setId(Packet.getNewPacketId());
		}
		if (msg.getBornTime() == null) {
			msg.setBornTime(System.currentTimeMillis());
		}

		if (debug) {
			log.info("[{}]: encode: {}", ctx.channel().remoteAddress(), GsonUtils.toString(msg));
		}

		byte[] data = ProtocolManager.encode(ctx.channel(), msg);
		if (data.length > SEND_MESSAGE_MAX_SIZE) {
			throw new PacketSizeException("默认发送消息最大值:" + SEND_MESSAGE_MAX_SIZE + ", 发送消息的大小:" + data.length + ",消息为:" + msg.getName());
		}
		int startIdx = out.writerIndex();
		ByteBufOutputStream bout = new ByteBufOutputStream(out);
		try {
			bout.write(LENGTH_PLACEHOLDER);
			bout.write(data);
		} finally {
			bout.close();
		}
		int endIdx = out.writerIndex();
		out.setInt(startIdx, endIdx - startIdx - 4);
	}
}

