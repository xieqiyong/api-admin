package com.hz.api.admin.model.vo;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserPasswordVO implements Serializable {

    private Long id;

    private Long userId;

    private String accountName;

    private String passwordScene;

    private String password;

    private String address;

    private Date modifyTime;

    private Date createTime;
}
