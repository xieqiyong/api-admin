package com.hz.api.admin.web.app;

import com.hz.api.admin.common.Result.ResultInfo;
import com.hz.api.admin.web.service.DataCenterService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/data")
public class DataCenterController {

    @Resource
    private DataCenterService dataCenterService;

    @GetMapping("/getRps")
    public ResultInfo getRps(@RequestParam("stressId") Long stressId,
                            @RequestParam("startTime") long startTime,
                             @RequestParam("endTime") long endTime){
        return dataCenterService.getRps(stressId, startTime, endTime);
    }
}
