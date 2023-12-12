package com.hz.api.admin.web.app;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.request.SceneRequest;
import com.hz.api.admin.web.service.ScriptService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/v1/script")
public class ScriptController {

    @Resource
    private ScriptService scriptService;
    /**
     * 上传脚本文件
     * @return
     */
    @PostMapping("/upload")
    public ResultInfo uploadScript(@RequestPart("file") MultipartFile file){
        return scriptService.uploadScript(file);
    }

    @GetMapping("/list")
    public ResultInfo scriptList(@RequestParam("filename") String filename){
        return scriptService.scriptList(filename);
    }

    @PostMapping("/delete")
    public ResultInfo deleteUserPassword(@RequestParam("ids") List<Long> ids){
        scriptService.deleteScript(ids);
        return ResultInfo.success();
    }

    @PostMapping("/scene/add")
    public ResultInfo addScene(@RequestBody SceneRequest sceneRequest){
        return scriptService.addScene(sceneRequest);
    }
}
