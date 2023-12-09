package com.hz.api.admin.web.service;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.request.SceneExecRequest;

public interface CommandService {

    public ResultInfo execStart(SceneExecRequest request);
}
