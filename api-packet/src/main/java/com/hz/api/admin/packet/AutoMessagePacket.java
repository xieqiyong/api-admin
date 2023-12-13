package com.hz.api.admin.packet;


import com.hz.api.admin.netkit.packet.Packet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xieqiyong66@gmail.com
 * @description: 用于服务端直接发送客户端消息
 * @date 2022/12/15 2:17 下午
 */
public class AutoMessagePacket extends Packet {

    public static final String NAME = "autoMessage";

    @Override
    public String getName() {
        return NAME;
    }

    public byte[] getBizData() {
        return bizData;
    }

    public void setBizData(byte[] bizData) {
        this.bizData = bizData;
    }

    /**
     * 服务端发送数据
     */
    private byte[] bizData;

    public AutoMessagePacket(){
        setType(Type.request);
    }

    @Override
    public Map<String, Object> getExtensionsData() {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getType() == Type.request) {
            map.put("data", bizData);
        } else if (getType() == Type.result) {
            map.put("result", "");
        }
        return map;
    }
}
