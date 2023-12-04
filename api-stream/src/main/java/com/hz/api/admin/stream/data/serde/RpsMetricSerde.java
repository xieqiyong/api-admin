package com.hz.api.admin.stream.data.serde;

import com.hz.api.admin.stream.data.serializer.FastJsonDeserializer;
import com.hz.api.admin.stream.data.serializer.FastJsonSerializer;
import com.hz.api.admin.stream.data.model.metric.RpsMetric;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;


public class RpsMetricSerde implements Serde<RpsMetric> {
    @Override
    public Serializer<RpsMetric> serializer() {
        return new FastJsonSerializer<>(RpsMetric.class);
    }

    @Override
    public Deserializer<RpsMetric> deserializer() {
        return new FastJsonDeserializer<>(RpsMetric.class);
    }
}
