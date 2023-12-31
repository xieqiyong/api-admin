package com.hz.api.admin.web.service;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.request.SceneRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ScriptService {

    ResultInfo uploadScript(MultipartFile file);

    ResultInfo scriptList(String filename);

    void deleteScript(List<Long> ids);

    ResultInfo addScene(SceneRequest sceneRequest);

    ResultInfo getSceneList();
}
