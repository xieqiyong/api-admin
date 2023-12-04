package com.hz.api.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "api_stress_data")
@TableName(value = "api_stress_data")
public class ApiStressDataEntity implements Serializable {

    private static final long serialVersionUID = 1969706676039515112L;
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type= IdType.AUTO)
    private Long id;

    @Column
    @Type(type="text")
    private String content;
}
