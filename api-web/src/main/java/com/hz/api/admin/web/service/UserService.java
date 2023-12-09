package com.hz.api.admin.web.service;

import com.github.pagehelper.PageInfo;
import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.entity.ApiUserPasswordEntity;
import com.hz.api.admin.model.entity.ApiUsersEntity;
import com.hz.api.admin.model.request.UserLoginRequest;
import com.hz.api.admin.model.request.UserPasswordRequest;
import com.hz.api.admin.model.vo.UserMenuVO;

import java.util.List;
import java.util.Map;

public interface UserService {

    PageInfo<ApiUserPasswordEntity> getUserPassword(int pageSize, int pageNum);

    void addUserPassword(UserPasswordRequest userPasswordRequest);

    void deleteUserPassword(List<Long> ids);

    Map userLogin(UserLoginRequest userLoginRequest);

    List<UserMenuVO> getUserMenu();

    ApiUsersEntity getUserInfoById(String userId);

    ResultInfo getUserInfo();
}
