package com.hz.api.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "api_scripts")
@TableName(value = "api_scripts")
public class ApiScriptsEntity implements Serializable {

    private static final long serialVersionUID = 6734297604641731406L;

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type= IdType.AUTO)
    private Long id;

    @Column
    private String scriptName;

    @Column
    private String scriptContent;

    @Column
    private Long userId;

    @Column
    private Long fileSize;

    @Column
    private String fileIndex;

    @Column
    private String fileExt;

    @Column
    private String charsetContent;

    @Column
    private Date modifyTime;

    @Column
    private Date createTime;
}
