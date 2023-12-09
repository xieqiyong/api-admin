package com.hz.api.admin.model.bo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ScriptListBO implements Serializable {
    private static final long serialVersionUID = -820003236219285006L;

    private Long id;

    private String scriptName;

    private String scriptContent;

    private Long userId;

    private BigDecimal fileSize;

    private String fileIndex;

    private String fileExt;

    private String charsetContent;

    private Date modifyTime;

    private Date createTime;
}
