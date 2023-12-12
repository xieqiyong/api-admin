package com.hz.api.admin.model.message;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xieqiyong66@gmail.com
 * @description: ClientInfo
 * @date 2022/11/7 6:44 下午
 */
public class ClientInfo implements Serializable {

    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime) {
        this.registerTime = registerTime;
    }

    /**
     * 主机名
     */
    private String hostName;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 注册时间
     */
    private Date registerTime;
}
