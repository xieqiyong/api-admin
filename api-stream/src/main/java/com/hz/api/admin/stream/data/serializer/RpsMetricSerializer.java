package com.hz.api.admin.stream.data.serializer;

import com.alibaba.fastjson.JSON;
import com.hz.api.admin.stream.data.model.metric.RpsMetric;
import org.apache.kafka.common.serialization.Serializer;

public class RpsMetricSerializer implements Serializer<RpsMetric> {

    @Override
    public byte[] serialize(String topic, RpsMetric data) {
        if (data == null) {
            return null;
        }
        return JSON.toJSONString(data).getBytes();
    }
}

