package com.hz.api.admin.engine.transport;

import com.hz.api.admin.engine.execute.MatchSourceExecutor;
import com.hz.api.admin.netkit.server.ConnectionManager;
import com.hz.api.admin.netkit.server.NetkitServer;
import com.hz.api.admin.netkit.server.PacketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liusu
 */
@Configuration
@Slf4j
public class EngineServerConfiguration implements ApplicationContextAware {

    public static final String ADVERTISED_ENDPOINT_KEY = "transport.advertisedEndpoint";

    private ApplicationContext applicationContext;

    @Value("${transport.listenerPort:15001}")
    private int    listenerPort;
    @Value("${transport.advertisedEndpoint:}")
    private String advertisedEndpoint;

    @Value("${netkit-server.packet-process.pool-core-size:8}")
    private int packetProcessPoolCoreSize;
    @Value("${netkit-server.packet-process.pool-max-size:16}")
    private int packetProcessPoolMaxSize;
    @Value("${netkit-server.packet-process.pool-queue-capacity:5000}")
    private int packetProcessPoolQueueCapacity;
    @Value("${netkit-server.connection-notify.pool-core-size:2}")
    private int connectionNotifyPoolCoreSize;
    @Value("${netkit-server.connection-notify.pool-max-size:4}")
    private int connectionNotifyPoolMaxSize;
    @Value("${netkit-server.connection-notify.pool-queue-capacity:200}")
    private int connectionNotifyPoolQueueCapacity;

    @Value("${clientManager.onOrOffline-executor.pool-core-size:8}")
    private int onOrOfflinePoolCoreSize;
    @Value("${clientManager.onOrOffline-executor.queue-size:500}")
    private int queueSize;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean(name = "clientMatchExecutor", destroyMethod = "stop")
    public MatchSourceExecutor matchSourceExecute() {
        // 负责客户端上线/离线处理线程池
        return new MatchSourceExecutor(onOrOfflinePoolCoreSize, queueSize, "onOrOffLineTask");
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor packetProcessExecutor() {
        // Java线程池实现原理及其在美团业务中的实践: https://tech.meituan.com/2020/04/02/java-pooling-pratice-in-meituan.html
        return new ThreadPoolExecutor(packetProcessPoolCoreSize, packetProcessPoolMaxSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(packetProcessPoolQueueCapacity), new CustomizableThreadFactory("NetkitPacketProcessExecutor-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolExecutor connectionNotifyExecutor() {
        return new ThreadPoolExecutor(connectionNotifyPoolCoreSize, connectionNotifyPoolMaxSize, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(connectionNotifyPoolQueueCapacity), new CustomizableThreadFactory("NetkitConnectionNotifyExecutor-"),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Bean("netkitConnectionManager")
    public ConnectionManager connectionManager(//
                                               @Qualifier("packetProcessExecutor") ThreadPoolExecutor packetProcessExecutor,//
                                               @Qualifier("connectionNotifyExecutor") ThreadPoolExecutor connectionNotifyExecutor) {
        return new ConnectionManager(packetProcessExecutor, connectionNotifyExecutor);
    }

    @Bean(destroyMethod = "shutdown")
    public NetkitServer netkitServer(ConnectionManager netkitConnectionManager) {
        Collection<PacketHandler> packetHandlers = applicationContext.getBeansOfType(PacketHandler.class).values();
        NetkitServer server = new NetkitServer(netkitConnectionManager);
        server.setPort(listenerPort);
        server.setChannelReadTimeoutMs(TimeUnit.MINUTES.toMillis(5));
        packetHandlers.forEach(server::addPacketHandler);
        server.start();
        log.info("通信端启动成功");
        return server;
    }
}