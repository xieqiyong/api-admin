package com.hz.api.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "api_stress_scene_exec")
@TableName(value = "api_stress_scene_exec")
public class ApiStressSceneExecEntity implements Serializable {

    private static final long serialVersionUID = -8004224972628001179L;

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type= IdType.AUTO)
    private Long id;

    @Column
    private String sceneId;

    @Column
    private Integer execState;

    @Column
    @Type(type = "text")
    private String extraContent;

    @Column
    private String startTime;

    @Column
    private String endTime;

    @Column
    private Long userId;

    @Column
    private Date modifyTime;

    @Column
    private Date createTime;
}
