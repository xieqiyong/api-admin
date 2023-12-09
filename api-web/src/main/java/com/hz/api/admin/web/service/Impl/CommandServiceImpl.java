package com.hz.api.admin.web.service.Impl;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.model.request.SceneExecRequest;
import com.hz.api.admin.web.service.CommandService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class CommandServiceImpl implements CommandService {
    @Override
    public ResultInfo execStart(SceneExecRequest request) {
        return null;
    }

    public String buildCommand(){
        StringBuilder command = new StringBuilder();
//        command.append(" JMETER_OPTS=\"-Dlog.root=").append(AssetUtil.getScriptLogHome(taskId))
//                .append(" -DRUN_TAG=").append(runTag == null ? "" : runTag)
//                .append(" -DreportId=").append(reportId)
//                .append(" -DtaskId=").append(taskId)
//                .append(" -Dcustom.host=").append(assetTaskRequestMsg.getCustomHost())// 用户自定义host
//                .append(" -Dcollector.host=").append(Constants.COLLECTOR_HOST)
//                .append(" -Drun.mode=").append(runMode)
//                .append(" -Dscript.type=").append(taskScriptType.getName())
//                .append(" -Dengine.log.level=").append(logLevel)
//                .append(" -Ddiff.time=").append(diffTime.toString())
//                .append(" -Dredis.host=").append(host)
//                .append(" -Dredis.port=").append(port)
//                .append(" -Dredis.password=").append(redisPassword);
//        if (StringUtils.isNotEmpty(assetTaskRequestMsg.getErrorKeywordPath())) {
//            command.append(" -DCalculateConfig=").append(assetTaskRequestMsg.getErrorKeywordPath());
//        }
//        command.append("\" ")
//                .append(" LANG=zh_CN.utf8 ")
//                .append(" sh ").append(Constants.JMETER_ENGINE_BOOTSTRAP)
//                .append(" -n ")
//                .append(" -Jsearch_paths=").append(AssetUtil.getUserLibDir(taskId))
//                .append(" -Jplugin_dependency_paths=").append(Constants.JMETER_PLUGIN_LIB)
//                .append(" -t ").append(AssetUtil.getScriptHome(taskId) + "/" + bootstrapName);

        return command.toString();
    }
}
