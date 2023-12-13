package com.hz.api.admin.packet;

import com.hz.api.admin.netkit.packet.Packet;
import org.nustaq.serialization.annotations.Version;

/**
 * @author xieqiyong66@gmail.com
 * @description: MessagePacket
 * @date 2022/11/8 10:39 上午
 */
public class MessagePacket extends Packet {

    private static final long serialVersionUID = -8797934349597848139L;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isAcks() {
        return acks;
    }

    public void setAcks(boolean acks) {
        this.acks = acks;
    }

    public boolean isByteArray() {
        return isByteArray;
    }

    public void setByteArray(boolean byteArray) {
        isByteArray = byteArray;
    }

    /**
     * 消息topic
     */
    private String  topic;
    /**
     * 消息内容
     */
    private byte[]  body;
    /**
     * 是否需要回复确认
     */
    private boolean acks;

    /**
     * 原始数据是否是字节数组
     */
    @Version(12)
    private boolean isByteArray;

    public static final String NAME = "message";

    public MessagePacket() {
        setType(Type.request);
    }


    @Override
    public String getName() {
        return NAME;
    }
}
