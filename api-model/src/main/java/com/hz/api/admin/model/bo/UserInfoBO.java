package com.hz.api.admin.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfoBO implements Serializable {

    private static final long serialVersionUID = -1246867881004670052L;

    private Long id;


    private String nickname;


    private String username;


    private String salt;


    private String password;


    private String picture;


    private Integer enableStatus;


    private Date modifyTime;


    private Date createTime;
}
