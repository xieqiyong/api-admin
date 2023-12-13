package com.hz.api.admin.packet;

import com.google.common.base.Optional;
import com.hz.api.admin.netkit.packet.Packet;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xieqiyong66@gmail.com
 * @description: CheckConnectionPacket
 * @date 2022/11/8 10:04 上午
 */
@Data
public class CheckConnectionPacket extends Packet {

    public static final String NAME = "checkConnection";
    private static final long serialVersionUID = 35196134751220288L;

    @Override
    public String getName() {
        return NAME;
    }

    public CheckConnectionPacket() {
        setType(Type.request);
    }

    /**
     * 连接凭证key
     */
    private String authKey;

    /**
     * 连接用户
     */
    private String authUser;

    /**
     * 客户端ID
     */
    private String clientId;

    private ProcessInfo processInfo;
    /**
     * 机器信息
     */
    private MachineInfo machineInfo;

    /**
     * 扩展数据
     */
    private Map<String, Object> extensions;

    @Override
    public Map<String, Object> getExtensionsData() {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getType() == Type.request) {
            map.put("clientId", clientId);
            map.put("processInfo", processInfo);
            map.put("machineInfo", machineInfo);
            map.put("extensions", Optional.fromNullable(extensions).or(new HashMap<String, Object>()));
        } else if (getType() == Type.result) {
            map.put("clientId", clientId);
        }
        return map;
    }

    @Data
    public static class ProcessInfo implements Serializable {

        private static final long serialVersionUID = 6931153357674382389L;
        // 进程ID
        private String pid;
        // 启动Class
        private String mainClass;
        // 启动参数
        private String mainArgs;
        // JVM参数
        private String jvmArgs;
        // JVM名称
        private String jvmName;
        // JVM版本
        private String jvmVersion;
        // 最大堆内存
        private Long maxHeapSize;
        // 最大非堆内存
        private Long maxNonHeapSize;
        // 启动时间
        private Long startTime;
        // 启动用户
        private String startUser;

    }

    @Data
    public static class MachineInfo implements Serializable {

        private static final long serialVersionUID = 6534300617991797075L;
        // IP地址
        private String ip;
        // Mac地址
        private String mac;
        // 主机名
        private String hostName;
        // 操作系统名称
        private String osName;
        // 操作系统架构
        private String osArch;
        // 操作系统版本
        private String osVersion;
        // CPU核数
        private Integer cpuCores;
        // 内存大小(bytes)
        private Long memorySize;
        // 磁盘总大小(bytes)
        private Long diskSize;
        // 虚拟机平台名称(unknown、vm、docker、k8s、mesos)
        private String osVmName;
    }
}
