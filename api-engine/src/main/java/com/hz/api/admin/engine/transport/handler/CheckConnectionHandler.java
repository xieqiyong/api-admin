package com.hz.api.admin.engine.transport.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.hz.api.admin.engine.transport.ClientConnectionManager;
import com.hz.api.admin.model.message.ClientInfo;
import com.hz.api.admin.netkit.exception.BizException;
import com.hz.api.admin.netkit.packet.Packet;
import com.hz.api.admin.netkit.packet.PacketError;
import com.hz.api.admin.netkit.server.ConnectionClosedException;
import com.hz.api.admin.netkit.server.NetkitConnection;
import com.hz.api.admin.packet.CheckConnectionPacket;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author liusu
 */
@Component
public class CheckConnectionHandler extends AbstractPacketHandler<CheckConnectionPacket> {

    private static final Logger log = LoggerFactory.getLogger(CheckConnectionHandler.class);


    @Resource
    private ClientConnectionManager clientConnectionManager;

    @Resource
    private Cache<String, Object> localCache;


    @Override
    public String getName() {
        return CheckConnectionPacket.NAME;
    }

    @Override
    public CheckConnectionPacket handle(NetkitConnection netkitConnection, CheckConnectionPacket packet) {
        if (netkitConnection.isClosed()) {
            log.info("客户端[{}/addr:{}]断开连接，中断上线处理流程", packet.getClientId(), netkitConnection.getHostAddress());
            return null;
        }
        CheckConnectionPacket reply = Packet.createResult(packet);
        try {
            reply.setClientId(packet.getClientId());
            reply.setExtensions(packet.getExtensions());
            reply.setMachineInfo(packet.getMachineInfo());
            reply.setProcessInfo(packet.getProcessInfo());

            ClientInfo clientInfo = new ClientInfo();
            clientInfo.setClientId(packet.getClientId());
            clientInfo.setRegisterTime(new Date());
            clientConnectionManager.clientConnection(netkitConnection, clientInfo);
            netkitConnection.bindProtocolHeader(packet.getHeader(), false);
            netkitConnection.sendPacket(reply).addListener(future -> {
                if (future.isSuccess()) {
                    ChannelPipeline p = netkitConnection.getChannel().pipeline();
                    p.remove(ReadTimeoutHandler.class);
                    p.addFirst(new ReadTimeoutHandler(30, TimeUnit.SECONDS));
                }
            });
            localCache.asMap().put(packet.getClientId(), netkitConnection.getChannelId());
        } catch (BizException e) {
            netkitConnection.sendPacket(reply).addListener((ChannelFutureListener) future -> netkitConnection.close());
        } catch (ConnectionClosedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
