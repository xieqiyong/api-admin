package com.hz.api.admin.model.vo;

import lombok.Data;


import java.io.Serializable;
import java.util.List;

@Data
public class UserMenuVO implements Serializable {

    private static final long serialVersionUID = -2033595354485430095L;

    private Long apikey;

    private Long apiParentKey;

    private String id;

    private String title;

    private String icon;

    private List<UserMenuVO> children;
}
