package com.hz.api.admin.web.service.Impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.common.exception.BizException;
import com.hz.api.admin.model.bo.ScriptListBO;
import com.hz.api.admin.model.dao.ApiScriptsDao;
import com.hz.api.admin.model.entity.ApiScriptsEntity;
import com.hz.api.admin.stream.util.FilePathUtils;
import com.hz.api.admin.web.config.thread.UserContextHolder;
import com.hz.api.admin.web.service.ScriptService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ScriptServiceImpl implements ScriptService {

    @Resource
    private ApiScriptsDao apiScriptsDao;

    @Override
    @Transactional
    public ResultInfo uploadScript(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String fileExt = "";
            String[] fileDetail = filename.split("\\.");
            filename = fileDetail[0];
            fileExt = fileDetail[1];
            // 文件是否重复
            ApiScriptsEntity apiScripts = new LambdaQueryChainWrapper<>(this.apiScriptsDao)
                    .eq(ApiScriptsEntity::getUserId, UserContextHolder.getUserId())
                    .eq(ApiScriptsEntity::getScriptName, filename)
                    .one();
            if(ObjectUtils.isNotEmpty(apiScripts)){
                throw new BizException("文件已存在, 请重新选择");
            }
            InputStream inputStream = file.getInputStream();
            String filePath = FilePathUtils.SCRIPT_ROOT_PATH + filename;
            FileUtil.writeFromStream(inputStream, new File(filePath));
            ApiScriptsEntity apiScriptsEntity = new ApiScriptsEntity();
            apiScriptsEntity.setScriptName(filename);
            apiScriptsEntity.setFileExt(fileExt);
            apiScriptsEntity.setFileSize(file.getSize());
            apiScriptsEntity.setCharsetContent(Charset.defaultCharset().name());
            apiScriptsEntity.setCreateTime(new Date());
            apiScriptsEntity.setUserId(UserContextHolder.getUserId());
            apiScriptsEntity.setFileIndex(filePath);
            apiScriptsDao.insert(apiScriptsEntity);
        }catch (Exception e){
            log.error("上传脚本文件失败: {}", e);
            return ResultInfo.buildFail(e.getMessage());
        }
        return ResultInfo.success();
    }

    @Override
    public ResultInfo scriptList(String filename) {
        LambdaQueryChainWrapper<ApiScriptsEntity> queryChainWrapper =  new LambdaQueryChainWrapper<>(this.apiScriptsDao)
                .eq(ApiScriptsEntity::getUserId, UserContextHolder.getUserId())
                .orderByDesc(ApiScriptsEntity::getId);
        if(StringUtils.isNotBlank(filename)){
            queryChainWrapper.like(ApiScriptsEntity::getScriptName, filename);
        }
        List<ApiScriptsEntity> list = queryChainWrapper.list();
        List<ScriptListBO> list1 = new ArrayList<>();
        list.forEach(v -> {
            ScriptListBO scriptListBO = new ScriptListBO();
            BeanUtils.copyProperties(v, scriptListBO);
            BigDecimal fileSize = new BigDecimal(v.getFileSize());
            BigDecimal finalSize = fileSize.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP);
            scriptListBO.setFileSize(finalSize);
            list1.add(scriptListBO);
        });
        return ResultInfo.success(list1);
    }

    @Override
    public void deleteScript(List<Long> ids) {
        QueryWrapper<ApiScriptsEntity> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", UserContextHolder.getUserId());
        queryWrapper.in("id", ids);
        this.apiScriptsDao.delete(queryWrapper);
    }
}
