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
@Table(name = "api_stress_data")
@TableName(value = "api_stress_data")
public class ApiStressMachineEntity implements Serializable {
    private static final long serialVersionUID = -8288491231256494183L;

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type= IdType.AUTO)
    private Long id;

    @Column
    private String hostname;

    @Column
    private String ip;

    @Column
    private Integer machineState;

    @Column
    private String extension;

    @Column
    private Date lastOfflineTime;

    @Column
    private Date onlineTime;

    @Column
    private Date modifyTime;

    @Column
    private Date createTime;
}
