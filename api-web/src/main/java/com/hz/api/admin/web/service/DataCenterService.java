package com.hz.api.admin.web.service;

import com.hz.api.admin.common.Result.ResultInfo;

public interface DataCenterService {

    ResultInfo getRps(long stressId, long startTime, long endTime);
}
