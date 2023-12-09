package com.hz.api.admin.stream.data.model.write;

import lombok.Data;

import java.io.Serializable;

@Data
public class RpsWrite implements Serializable {

    private static final long serialVersionUID = 1749947924564385996L;

    private long count;

    private long startTime;

    private long endTime;

    private long stressId;

    private String key;

    private long ts;

    private boolean successful;

    private String sampleLabel;

    private long errCount;

    private long successCount;

    private long time;
}
