package com.hz.api.admin.engine.transport.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.perfma.xsea.data.collector.base.packet.CheckConnectionPacket;
import com.perfma.xsea.data.collector.exception.DataServerException;
import com.perfma.xsea.data.collector.model.ClientInfo;
import com.perfma.xsea.data.collector.model.CollectorErrorEnum;
import com.perfma.xsea.data.collector.netkit.packet.Packet;
import com.perfma.xsea.data.collector.netkit.packet.PacketError;
import com.perfma.xsea.data.collector.netkit.server.ConnectionClosedException;
import com.perfma.xsea.data.collector.netkit.server.NetkitConnection;
import com.perfma.xsea.data.collector.transport.ClientConnectionManager;
import com.perfma.xsea.data.collector.utils.Security;
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
            String authKey = packet.getAuthKey();
            if (StringUtils.isBlank(authKey)) {
                reply.setError(PacketError.buildError(CollectorErrorEnum.AUTH_FAILED.getErrorCode(), CollectorErrorEnum.AUTH_FAILED.getErrorMsg()));
                netkitConnection.sendPacket(reply).addListener((ChannelFutureListener) future -> netkitConnection.close());
            }
            if (Boolean.FALSE.equals(Security.checkUserSecurityValue(packet.getAuthUser(), authKey))) {
                reply.setError(PacketError.buildError(CollectorErrorEnum.AUTH_FAILED.getErrorCode(), CollectorErrorEnum.AUTH_FAILED.getErrorMsg()));
                netkitConnection.sendPacket(reply).addListener((ChannelFutureListener) future -> netkitConnection.close());
            }
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
        } catch (DataServerException | ConnectionClosedException e) {
            reply.setError(PacketError.buildError(CollectorErrorEnum.AUTH_FAILED.getErrorCode(), CollectorErrorEnum.AUTH_FAILED.getErrorMsg()));
            netkitConnection.sendPacket(reply).addListener((ChannelFutureListener) future -> netkitConnection.close());
        }
        return null;
    }
}
