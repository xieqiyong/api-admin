package com.hz.api.admin.model.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hz.api.admin.model.entity.ApiUserPasswordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiUserPasswordDao extends BaseMapper<ApiUserPasswordEntity> {
}
