package com.hz.api.admin.stream.data.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.hz.api.admin.stream.data.model.metric.RpsMetric;
import org.apache.kafka.common.serialization.Deserializer;

public class RpsMetricDeserializer implements Deserializer<RpsMetric> {
    @Override
    public RpsMetric deserialize(String s, byte[] bytes) {
        return JSON.parseObject(bytes, RpsMetric.class, Feature.DisableCircularReferenceDetect);
    }
}
