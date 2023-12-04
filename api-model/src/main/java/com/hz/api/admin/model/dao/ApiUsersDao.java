package com.hz.api.admin.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hz.api.admin.model.entity.ApiUsersEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiUsersDao extends BaseMapper<ApiUsersEntity> {

}
