package com.hz.api.admin.model.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class UserPasswordRequest implements Serializable {

    @NotBlank(message = "姓名不能为空")
    private String username;

    @NotBlank(message = "账号不能为空")
    private String accountName;

    @NotBlank(message = "使用场景不能为空")
    private String passwordScene;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String address;
}
