package com.hz.api.admin.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 143174394612672073L;

    private String account;

    private String password;
}
