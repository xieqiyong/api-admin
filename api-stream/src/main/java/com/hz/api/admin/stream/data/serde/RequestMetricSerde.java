package com.hz.api.admin.stream.data.serde;

import com.hz.api.admin.stream.data.model.metric.RequestMetric;
import com.hz.api.admin.stream.data.serializer.FastJsonDeserializer;
import com.hz.api.admin.stream.data.serializer.FastJsonSerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class RequestMetricSerde implements Serde<RequestMetric> {
    @Override
    public Serializer<RequestMetric> serializer() {
        return new FastJsonSerializer<>(RequestMetric.class);
    }

    @Override
    public Deserializer<RequestMetric> deserializer() {
        return new FastJsonDeserializer<>(RequestMetric.class);
    }
}
