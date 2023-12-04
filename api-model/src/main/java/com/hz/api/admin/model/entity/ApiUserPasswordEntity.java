package com.hz.api.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "api_user_password")
@TableName(value = "api_user_password")
@Entity
@Data
public class ApiUserPasswordEntity implements Serializable {
    private static final long serialVersionUID = -5922601592848313396L;
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type= IdType.AUTO)
    private Long id;

    @Column
    private String username;

    @Column
    private String accountName;

    @Column()
    private String passwordScene;

    @Column
    private String password;

    @Column
    private String address;

    @Column
    private Date modifyTime;

    @Column
    private Date createTime;
}
