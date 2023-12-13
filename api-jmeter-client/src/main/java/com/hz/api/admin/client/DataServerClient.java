package com.hz.api.admin.client;

import com.hz.api.admin.client.client.message.MessageManager;
import com.hz.api.admin.client.server.AutoMessageManager;
import com.hz.api.admin.model.enums.ClientState;
import com.hz.api.admin.netkit.client.ConnectionListenerAdapter;
import com.hz.api.admin.netkit.client.NetkitClient;
import com.hz.api.admin.netkit.encrypt.EncryptManager;
import com.hz.api.admin.netkit.listener.ReplyListener;
import com.hz.api.admin.packet.CheckConnectionPacket;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xieqiyong66@gmail.com
 * @description: DataServerClient
 * @date 2022/11/8 4:17 下午
 */
public class DataServerClient {

    private static final Logger log = LoggerFactory.getLogger(DataServerClient.class);
    private NetkitClient netkitClient;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final String clientId;
    private final Configuration configuration;
    private final Set<ClientStateListener> clientStateListeners = new CopyOnWriteArraySet<ClientStateListener>();
    private final AtomicBoolean registered = new AtomicBoolean(false);
    private final AtomicBoolean registering = new AtomicBoolean(false);
    private final ExecutorService executorService = Executors.newFixedThreadPool(1,
            new DefaultThreadFactory("ClientStateNotifyExecutor"));
    private final Map<Class<?>, AbstractDataModule> modules = new HashMap<Class<?>, AbstractDataModule>();

    /**
     * 注册超时时间
     */
    private long registrationTimeoutMs = TimeUnit.SECONDS.toMillis(60);

    public Set<ClientStateListener> getClientStateListeners() {
        return Collections.unmodifiableSet(clientStateListeners);
    }

    /**
     * 添加客户端状态监听器
     */
    public void addClientStateListener(ClientStateListener listener) {
        Objects.requireNonNull(listener, "listener must not be null");
        clientStateListeners.add(listener);
    }

    public DataServerClient(Configuration configuration) {
        this.clientId = StringUtils.isBlank(configuration.getClientId()) ?
                genClientId() : configuration.getClientId();
        this.configuration = configuration;
    }

    /**
     * 获取底层网络通信客户端实例
     */
    public NetkitClient getNetkitClient() {
        return netkitClient;
    }

    /**
     * 获取客户端ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 获取客户端连接配置
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 是否已连接到服务端
     */
    public boolean isConnected() {
        return netkitClient != null && netkitClient.isConnected();
    }

    /**
     * 是否已注册到服务端
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isRegistered() {
        return isConnected() && registered.get();
    }

    public AutoMessageManager getAutoMessageManager(){
        return (AutoMessageManager) modules.get(AutoMessageManager.class);
    }

    public MessageManager getMessageManager() {
        return (MessageManager) modules.get(MessageManager.class);
    }

    /**
     * 生成客户端ID
     */
    private String genClientId() {
        return "125152141234123@wdadsa";
    }

    /**
     * 启动客户端连接
     */
    public void start() {
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("DataServerClient already started");
        }

        if (configuration.getRegistrationTimeoutMs() != null && configuration.getRegistrationTimeoutMs() > 0) {
            registrationTimeoutMs = configuration.getRegistrationTimeoutMs();
        }
        configuration.setProtocol("netkit");
        configuration.setSerialization("fst");
        List<InetSocketAddress> addresses = parseServers(configuration.getServerAddress());
        netkitClient = buildNetkitClient(configuration);
        netkitClient.setServerAddresses(addresses);
        netkitClient.addConnectionListener(new ConnectionListenerImpl());

        // Load all the modules
        loadModules();
        // Initize all the modules
        initModules();
        // Start all the modules
        startModules();

        // connect
        netkitClient.connect();
    }

    /**
     * 启动管理器模块
     */
    private void startModules() {
        for (AbstractDataModule module : modules.values()) {
            try {
                log.info("Starting module: " + module.getClass().getName());
                module.start();
            } catch (Throwable e) {
                log.error("An exception occurred while starting module '{}'.", module.getClass().getName(), e);
                throw new IllegalStateException("An exception occurred while starting module '" + module.getClass().getName() + "'.", e);
            }
        }
    }

    /**
     * 初始化管理器模块
     */
    private void initModules() {
        for (AbstractDataModule module : modules.values()) {
            try {
                log.info("Initializing module: " + module.getClass().getName());
                module.initialize(this);
            } catch (Throwable e) {
                log.error("An exception occurred while initialize module '{}'.", module.getClass().getName(), e);
                throw new IllegalStateException("An exception occurred while initialize module '" + module.getClass().getName() + "'.", e);
            }
        }
    }

    /**
     * 加载管理器模块
     */
    private void loadModules() {
        // Load boot modules
        loadModule(MessageManager.class);
        loadModule(AutoMessageManager.class);
    }

    /**
     * 实例化管理器模块
     */
    private void loadModule(Class<? extends AbstractDataModule> moduleClass) {
        try {
            log.info("Loading module: " + moduleClass.getName());
            AbstractDataModule module = moduleClass.newInstance();
            this.modules.put(moduleClass, module);
        } catch (Throwable e) {
            log.error("An exception occurred while loading module '{}'.", moduleClass.getName(), e);
            throw new IllegalStateException("An exception occurred while loading module '" + moduleClass.getName() + "'.", e);
        }
    }

    /**
     * 解析服务端连接地址
     */
    private List<InetSocketAddress> parseServers(String servers) {
        Set<String> addresses = new HashSet<String>();
        for (String str : servers.split(",")) {
            String value = StringUtils.trimToNull(str);
            if (value != null) {
                addresses.add(value);
            }
        }

        if (addresses.isEmpty()) {
            throw new IllegalArgumentException("Must be defined as 'host:port,host:port,host:port'");
        }

        List<InetSocketAddress> inetSocketAddresses = new ArrayList<InetSocketAddress>();
        for (String address : addresses) {
            String[] parts = address.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Must be defined as 'host:port,host:port,host:port'");
            }
            inetSocketAddresses.add(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
        }
        return inetSocketAddresses;
    }

    /**
     * 构建底层网络通信客户端实例
     */
    private NetkitClient buildNetkitClient(Configuration configuration) {
        NetkitClient netkitClient = new NetkitClient();
        // 设置通信协议
        netkitClient.setProtocol(configuration.getProtocol());
        // 设置序列化类型
        netkitClient.setSerialization(configuration.getSerialization());
        // 是否启用重连
        if (configuration.getReconnectEnable() != null) {
            netkitClient.setReconnectEnable(configuration.getReconnectEnable());
        }
        // 重连时间间隔
        if (configuration.getReconnectIntervalMs() != null) {
            netkitClient.setReconnectIntervalMs(configuration.getReconnectIntervalMs());
        }
        // 连接超时时间
        if (configuration.getConnectionTimeoutMs() != null) {
            netkitClient.setConnectionTimeoutMs(configuration.getConnectionTimeoutMs());
        }
        // 心跳包发送间隔时间
        if (configuration.getHeartbeatTimeSeconds() != null) {
            netkitClient.setHeartbeatTimeSeconds(configuration.getHeartbeatTimeSeconds());
        }
        // 数据包回复超时时间
        if (configuration.getPacketReplyTimeoutMs() != null) {
            netkitClient.setPacketReplyTimeoutMs(configuration.getPacketReplyTimeoutMs());
        }
        if (configuration.getMaxAsyncTasks() != null) {
            netkitClient.setMaxAsyncTasks(configuration.getMaxAsyncTasks());
        }
        return netkitClient;
    }

    /**
     * 注册客户端到服务
     */
    private synchronized void register() {
        if (!isConnected() || isRegistered() || !registering.compareAndSet(false, true)) {
            return;
        }

        // 初始化configuration extension配置
        final CheckConnectionPacket packet = new CheckConnectionPacket();
        // 设置authkey
        packet.setAuthKey(this.configuration.getAuthKey());
        packet.setAuthUser(this.configuration.getAuthUser());
        packet.setClientId(clientId);
        netkitClient.sendPacket(packet, packet.createReplyFilter(), new ReplyListener<CheckConnectionPacket>() {
            @Override
            public void onPacket(CheckConnectionPacket packet) {
                registering.set(false);
                log.info("注册当前客户端到 {} 成功: {}", netkitClient.remoteAddress(), packet);

                // 设置当前状态为已注册
                DataServerClient.this.registered.set(true);
                // 调用客户端状态监听器
                invokeClientStateNotifyListeners(ClientState.REGISTERED);
            }

            @Override
            public void onFailure(Throwable e) {
                registering.set(false);
                log.error("注册当前客户端到 {} 失败: {}", netkitClient.remoteAddress(), packet, e);

                if (isConnected()) {
                    // 连接失败后自动重连
                    try {
                        TimeUnit.MILLISECONDS.sleep(5000);
                        register();
                    } catch (InterruptedException ex) {
                        log.error("Interrupted", ex);
                    }
                }
            }
        }, registrationTimeoutMs);
    }

    /**
     * 客户端状态改变通知
     */
    void invokeClientStateNotifyListeners(final ClientState state) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (ClientStateListener listener : getClientStateListeners()) {
                    try {
                        listener.stateChanged(DataServerClient.this, state);
                    } catch (Throwable e) {
                        log.error("An exception was thrown by {}.stateChanged({})", listener.getClass().getName(), state, e);
                    }
                }
            }
        });
    }

    /**
     * 服务端连接状态监听器
     */
    class ConnectionListenerImpl extends ConnectionListenerAdapter {

        /**
         * 与服务端建立连接成功
         */
        @Override
        public void channelConnected(Channel channel) {
            // 调用客户端状态监听器
            invokeClientStateNotifyListeners(ClientState.CONNECTED);
            // 注册当前客户端
            register();
        }

        /**
         * 与服务端连接断开
         */
        @Override
        public void channelClosed(Channel channel) {
            // 设置当前状态为未注册
            DataServerClient.this.registered.set(false);
            // 移除channel对应的加密key
            EncryptManager.removeEncrypt(channel.id().asLongText());
            // 调用客户端状态监听器
            invokeClientStateNotifyListeners(ClientState.DISCONNECT);
        }
    }
}
