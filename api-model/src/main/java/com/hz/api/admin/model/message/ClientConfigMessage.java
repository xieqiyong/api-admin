package com.hz.api.admin.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class ClientConfigMessage implements Serializable {
    private static final long serialVersionUID = 5667256104670992474L;

    private String clientId;

    private String machineIp;

    private Long registerTime;
}
