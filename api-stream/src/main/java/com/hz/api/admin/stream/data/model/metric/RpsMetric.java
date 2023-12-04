package com.hz.api.admin.stream.data.model.metric;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpsMetric implements Serializable {


    private static final long serialVersionUID = -2739659255877401762L;

    private Long count = 1L;

    private Long errCount = 1L;

    private long startTime;

    private long endTime;

    private String key;

    private boolean successful;

    private String sampleLabel;

    private long errorCount;

    public RpsMetric add(RpsMetric res) {
        this.count ++;
        this.endTime = res.getEndTime();
        return this;
    }

    public RpsMetric addErr(RpsMetric res){
        this.errCount ++;
        this.endTime = res.getEndTime();
        return this;
    }
}
