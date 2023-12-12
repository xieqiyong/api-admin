package com.hz.api.admin.engine.transport;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.hz.api.admin.model.message.ClientInfo;
import com.hz.api.admin.netkit.server.ConnectionClosedException;
import com.hz.api.admin.netkit.server.ConnectionListenerAdapter;
import com.hz.api.admin.netkit.server.ConnectionManager;
import com.hz.api.admin.netkit.server.NetkitConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Redisson使用文档: https://github.com/redisson/redisson/wiki/1.-概述
 *
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/24.
 */
@Component
public class ClientConnectionManager extends ConnectionListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClientConnectionManager.class);

    @Resource
    private Cache<String, Object> localCache;

    public Map<String, ClientInfo> channelIdClientInfoMap;

    @Resource
    private ConnectionManager connectionManager;

    private final CountDownLatch startedDownLatch = new CountDownLatch(1);

    /**
     * 根据channelId获取客户端信息
     */
    public ClientInfo getClientInfoByChannelId(String channelId) {
        return channelIdClientInfoMap.get(channelId);
    }


    @PostConstruct
    public void init() {
        channelIdClientInfoMap = new ConcurrentHashMap<>();
        // 注册客户端连接监听器
        connectionManager.addConnectionListener(this);
        startedDownLatch.countDown();
    }

    /**
     * 根据channelId获取连接信息
     */
    public NetkitConnection getConnectionByChannelId(String channelId) {
        return connectionManager.getConnection(channelId);
    }

    /**
     * 客户端离线处理
     */
    @Override
    public void removed(NetkitConnection connection) {
        try {
            startedDownLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        String remoteAddress = connection.getHostAddress();
        ClientInfo clientInfo = channelIdClientInfoMap.get(connection.getChannelId());
        log.info("接收客户端[addr:{}]离线通知，提交客户端离线处理任务", remoteAddress);
        channelIdClientInfoMap.remove(connection.getChannelId());
        localCache.asMap().remove(clientInfo.getClientId());
        log.info("执行客户端[{}/addr:{}]离线处理成功: {}", connection.getChannelId(), remoteAddress, JSON.toJSONString(clientInfo));
    }

    /**
     * 客户端上线处理
     *
     * @param connection
     * @return
     */
    public ClientInfo clientConnection(NetkitConnection connection, ClientInfo clientInfo) throws ConnectionClosedException {
        try {
            startedDownLatch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        String clientId = clientInfo.getClientId();
        if (connection.isClosed()) {
            throw new ConnectionClosedException(connection);
        }
        String remoteAddress = connection.getHostAddress();
        log.info("接收到客户端[{}/addr:{}]上线通知，开始执行客户端上线处理流程: {}", clientInfo.getClientId(), remoteAddress, JSON.toJSONString(clientInfo));
        clientInfo.setRegisterTime(new Date());
        clientInfo.setHostName(connection.getHostName());
        clientInfo.setIp(connection.getHostAddress().toString());
        channelIdClientInfoMap.put(connection.getChannelId(), clientInfo);
        log.info("执行客户端[{}/addr:{}]上线处理流程成功: {}", clientId, remoteAddress, JSON.toJSONString(clientInfo));
        return clientInfo;
    }
}
