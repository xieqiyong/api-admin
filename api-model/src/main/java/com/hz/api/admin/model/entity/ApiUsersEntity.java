package com.hz.api.admin.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name = "api_users")
@TableName("api_users")
@Entity
@Data
public class ApiUsersEntity implements Serializable {
    private static final long serialVersionUID = -5922601592848313396L;
    @Id
    @Column(nullable = false)
    private Long id;

    @Column
    private String nickname;

    @Column()
    private String username;

    @Column
    private String salt;

    @Column
    private String password;

    @Column
    private String picture;

    @Column
    private Integer enableStatus;

    @Column
    private Date modifyTime;

    @Column
    private Date createTime;
}
