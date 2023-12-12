package com.hz.api.admin.netkit.codec;


import com.hz.api.admin.netkit.exception.BizException;
import com.hz.api.admin.netkit.packet.Packet;

public class PacketSizeException extends BizException {

    public PacketSizeException(Packet packet) {
        super(packet);
    }

    public PacketSizeException(String message) {
        super(message);
    }

}
