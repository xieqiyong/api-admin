package com.hz.api.admin.model.vo;

import lombok.Data;

@Data
public class RpsVO {

    private long count;

    private long errCount;

    private long startTime;

    private long endTime;

    private long stressId;

    private String key;

    private long ts;

    private String sampleLabel;

    private boolean successful;

    private Long successCount;

    private long time;

    public long getTime(){
        return time / count;
    }
}
