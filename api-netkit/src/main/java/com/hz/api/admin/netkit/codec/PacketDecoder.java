/*
 * Created by zhangzxiang91@gmail.com on 2021/05/07.
 */
package com.hz.api.admin.netkit.codec;


import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.protocol.ProtocolManager;
import com.hz.api.admin.netkit.utils.GsonUtils;
import com.hz.api.admin.netkit.utils.SystemUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/07.
 */
@Slf4j
public class PacketDecoder extends LengthFieldBasedFrameDecoder {

	// 单个数据包大小限制，默认30M
	private static final int     defaultMaxFrameLength = Integer.parseInt(SystemUtils.getProp("netkit.decoder.defaultMaxFrameLength", "31457280"));
	private static final boolean debug                 = Boolean.parseBoolean(SystemUtils.getProp("netkit.decoder.debug", "false"));

	public PacketDecoder() {
		this(defaultMaxFrameLength);
	}

	public PacketDecoder(int maxFrameLength) {
		super(maxFrameLength, 0, 4, 0, 4);
	}

	public PacketDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip,
			boolean failFast) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		ByteBuf frame = (ByteBuf)super.decode(ctx, in);
		if (frame == null) {
			return null;
		}

		try {
			Packet packet = ProtocolManager.decode(ctx.channel(), frame);
			if (debug) {
				log.info("[{}]: decode: Packet{}", ctx.channel().remoteAddress(), GsonUtils.toString(packet));
			}

			return packet;
		} catch (Throwable e) {
			log.error("Packet decode error, packet={}", frame.toString(Charsets.UTF_8), e);
			return null;
		} finally {
			frame.release();
		}
	}
}

