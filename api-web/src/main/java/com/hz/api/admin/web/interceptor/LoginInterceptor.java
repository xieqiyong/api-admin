package com.hz.api.admin.web.interceptor;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.hz.api.admin.model.bo.UserInfoBO;
import com.hz.api.admin.model.entity.ApiUsersEntity;
import com.hz.api.admin.web.config.thread.UserContextHolder;
import com.hz.api.admin.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            response.setStatus(401);
            return false;
        }
        JWT jwt = JWTUtil.parseToken(token);
        String userId = jwt.getPayload("userId").toString();
        ApiUsersEntity apiUsersEntity = userService.getUserInfoById(userId);
        if (ObjectUtils.isEmpty(apiUsersEntity)) {
            response.setStatus(403);
            return false;
        }
        UserInfoBO userInfoBO = new UserInfoBO();
        BeanUtils.copyProperties(apiUsersEntity, userInfoBO);
        UserContextHolder.setContext(userInfoBO);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {

    }
}
