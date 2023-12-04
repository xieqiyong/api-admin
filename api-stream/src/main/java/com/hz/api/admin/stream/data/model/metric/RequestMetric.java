package com.hz.api.admin.stream.data.model.metric;

import lombok.Data;

@Data
public class RequestMetric {

    private long startTime;

    private boolean successful;

    private Long mesh;

    private long endTime;

    private String sampleLabel;

    private String urlAsString;

    private long errorCount;

}
