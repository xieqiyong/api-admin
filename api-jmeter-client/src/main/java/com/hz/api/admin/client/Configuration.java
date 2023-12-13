package com.hz.api.admin.client;


import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/06/16.
 */
public class Configuration extends HashMap<String, String> {

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public Long getPacketReplyTimeoutMs() {
        return packetReplyTimeoutMs;
    }

    public void setPacketReplyTimeoutMs(Long packetReplyTimeoutMs) {
        this.packetReplyTimeoutMs = packetReplyTimeoutMs;
    }

    public Long getHeartbeatTimeSeconds() {
        return heartbeatTimeSeconds;
    }

    public void setHeartbeatTimeSeconds(Long heartbeatTimeSeconds) {
        this.heartbeatTimeSeconds = heartbeatTimeSeconds;
    }

    public Long getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(Long connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public Long getRegistrationTimeoutMs() {
        return registrationTimeoutMs;
    }

    public void setRegistrationTimeoutMs(Long registrationTimeoutMs) {
        this.registrationTimeoutMs = registrationTimeoutMs;
    }

    public Boolean getReconnectEnable() {
        return reconnectEnable;
    }

    public void setReconnectEnable(Boolean reconnectEnable) {
        this.reconnectEnable = reconnectEnable;
    }

    public Long getReconnectIntervalMs() {
        return reconnectIntervalMs;
    }

    public void setReconnectIntervalMs(Long reconnectIntervalMs) {
        this.reconnectIntervalMs = reconnectIntervalMs;
    }

    public Integer getMaxAsyncTasks() {
        return maxAsyncTasks;
    }

    public void setMaxAsyncTasks(Integer maxAsyncTasks) {
        this.maxAsyncTasks = maxAsyncTasks;
    }

    public ExtensionLoader getExtensionLoader() {
        return extensionLoader;
    }

    public void setExtensionLoader(ExtensionLoader extensionLoader) {
        this.extensionLoader = extensionLoader;
    }

    // 客户端类型
    private String clientType;
    // 远程服务端地址
    private String serverAddress;
    // 数据通信协议
    private String protocol;
    // 数据序列化类型
    private String serialization;
    // 等待数据包返回超时时间
    private Long packetReplyTimeoutMs;
    // 心跳数据包发送间隔时间
    private Long heartbeatTimeSeconds;
    // 与服务端建立连接超时时间
    private Long connectionTimeoutMs;
    // 注册超时时间
    private Long registrationTimeoutMs;
    // 是否启用连接断开重连机制
    private Boolean reconnectEnable;
    // 连接断开重连间隔时间
    private Long reconnectIntervalMs;
    // 异步任务并发个数
    private Integer maxAsyncTasks;
    // extension加载器
    private ExtensionLoader extensionLoader;
    // 链接认证凭证
    private String authKey;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    private String clientId;

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    // 认证用户
    private String authUser;

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }


    public Map<String, Object> getExtensions() {
        return this.extensionLoader.getExtensions();
    }

    public interface ExtensionLoader {

        Map<String, Object> getExtensions();
    }
}
