package com.hz.api.admin.web.app;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.request.SceneExecRequest;
import com.hz.api.admin.web.service.CommandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/command")
public class CommandController {

    @Resource
    private CommandService commandService;

    @RequestMapping("/scene/start")
    public ResultInfo startExec(@RequestBody SceneExecRequest request){
        return commandService.execStart(request);
    }
}
