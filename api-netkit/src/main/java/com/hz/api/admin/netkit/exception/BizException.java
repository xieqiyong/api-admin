package com.hz.api.admin.netkit.exception;


import com.hz.api.admin.netkit.packet.Packet;

public class BizException extends RuntimeException {

    private Packet packet;

    public BizException(Packet packet) {
        super();
        this.packet = packet;
    }

    public BizException(String message) {
        super(message);
    }

    public Packet getPacket() {
        return packet;
    }
}
