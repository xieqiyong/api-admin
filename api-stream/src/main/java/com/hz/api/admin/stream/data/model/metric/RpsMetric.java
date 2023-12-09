package com.hz.api.admin.stream.data.model.metric;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpsMetric implements Serializable {


    private static final long serialVersionUID = -2739659255877401762L;

    private Long count = 1L;

    private Long errCount = 0L;

    private long startTime;

    private long endTime;

    private String key;

    private boolean successful;

    private String sampleLabel;

    private Long successCount = 0L;

    private long time;

    public RpsMetric add(RpsMetric res) {
        if(this.isSuccessful()){
            this.successCount ++;
        }else{
            this.errCount ++;
        }
        this.time = this.time + res.getTime();
        this.count ++;
        this.endTime = res.getEndTime();
        return this;
    }
}
