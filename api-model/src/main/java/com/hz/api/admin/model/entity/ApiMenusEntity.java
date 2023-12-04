package com.hz.api.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "api_menus")
@TableName(value = "api_menus")
public class ApiMenusEntity implements Serializable {
    private static final long serialVersionUID = 7026589524286430409L;

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type= IdType.AUTO)
    private Long apikey;

    @Column
    private Long apiParentKey;

    @Column
    private String id;

    @Column
    private String icon;

    @Column
    private String title;

    @Column
    private String userId;

    @Column
    private boolean apiDisable;
}
