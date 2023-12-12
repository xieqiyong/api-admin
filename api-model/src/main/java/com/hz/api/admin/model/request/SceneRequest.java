package com.hz.api.admin.model.request;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class SceneRequest implements Serializable {
    private static final long serialVersionUID = -8246778936495674829L;

    @NotBlank(message = "场景名称不能为空")
    private String sceneName;

    @NotNull(message = "脚本ID不能为空")
    private Long scriptId;

    private int sceneType;

    @Size(min = 0)
    private Long execTime;
}
