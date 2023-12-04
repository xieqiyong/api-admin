package com.hz.api.admin.web.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hz.api.admin.common.exception.BizException;
import com.hz.api.admin.model.dao.ApiUserMenuDao;
import com.hz.api.admin.model.dao.ApiUserPasswordDao;
import com.hz.api.admin.model.dao.ApiUsersDao;
import com.hz.api.admin.model.entity.ApiMenusEntity;
import com.hz.api.admin.model.entity.ApiUserPasswordEntity;
import com.hz.api.admin.model.entity.ApiUsersEntity;
import com.hz.api.admin.model.request.UserLoginRequest;
import com.hz.api.admin.model.request.UserPasswordRequest;
import com.hz.api.admin.model.vo.UserMenuVO;
import com.hz.api.admin.web.config.thread.UserContextHolder;
import com.hz.api.admin.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private ApiUserPasswordDao apiUserPasswordDao;

    @Resource
    private ApiUsersDao apiUsersDao;

    @Resource
    private ApiUserMenuDao apiUserMenuDao;

    @Override
    public PageInfo<ApiUserPasswordEntity> getUserPassword(int pageSize, int pageNum) {
        QueryWrapper queryWrapper = new QueryWrapper();
        return PageHelper.startPage(pageNum, pageSize)
                .doSelectPageInfo(()->apiUserPasswordDao.selectList(queryWrapper));
    }
    @Override
    public void addUserPassword(UserPasswordRequest userPasswordRequest){
        ApiUserPasswordEntity apiUserPasswordEntity = new ApiUserPasswordEntity();
        BeanUtils.copyProperties(userPasswordRequest, apiUserPasswordEntity);
        apiUserPasswordEntity.setCreateTime(new Date());
        apiUserPasswordDao.insert(apiUserPasswordEntity);
    }

    @Override
    @Transactional
    public void deleteUserPassword(List<Long> ids) {
        this.apiUserPasswordDao.deleteBatchIds(ids);
    }

    @Override
    public Map<String, Object> userLogin(UserLoginRequest request) {
         ApiUsersEntity apiUsersEntity =
                 new LambdaQueryChainWrapper<>(this.apiUsersDao)
                         .eq(ApiUsersEntity::getPassword, request.getPassword())
                         .eq(ApiUsersEntity::getUsername, request.getAccount())
                         .one();
        if(ObjectUtil.isEmpty(apiUsersEntity)){
            throw new BizException("登陆失败, 请检查账号密码");
        }
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap();
        map.put("userId", apiUsersEntity.getId());
        String token = JWTUtil.createToken(map, apiUsersEntity.getSalt().getBytes());
        map.put("token",token);
        return map;
    }

    @Override
    public List<UserMenuVO> getUserMenu() {
        List<ApiMenusEntity> list = new LambdaQueryChainWrapper<>(this.apiUserMenuDao)
                .eq(ApiMenusEntity::getUserId, UserContextHolder.getUserId())
                .eq(ApiMenusEntity::isApiDisable, Boolean.TRUE)
                .list();

        List<UserMenuVO> voList = new ArrayList<>();
        for (ApiMenusEntity menus : list) {
            UserMenuVO userMenuVO = new UserMenuVO();
            BeanUtils.copyProperties(menus, userMenuVO);
            voList.add(userMenuVO);
        }
        List<UserMenuVO> hierarchy = buildTree(voList, 0L);
        return hierarchy;
    }

    @Override
    public ApiUsersEntity getUserInfoById(String userId) {
        return new LambdaQueryChainWrapper<>(this.apiUsersDao)
                .eq(ApiUsersEntity::getId, userId)
                .one();
    }

    public List<UserMenuVO> buildTree(List<UserMenuVO> list,
                                      Long parentId){
        List<UserMenuVO> hierarchy = new ArrayList<>();
        for (UserMenuVO menus : list) {
            if(menus.getApiParentKey().equals(parentId)){
                menus.setChildren(buildTree(list, menus.getApikey()));
                hierarchy.add(menus);
                if(CollectionUtil.isEmpty(menus.getChildren())){
                    menus.setChildren(null);
                }
            }
        }
        return hierarchy;
    }
}
