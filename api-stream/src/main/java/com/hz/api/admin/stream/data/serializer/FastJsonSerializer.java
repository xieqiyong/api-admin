package com.hz.api.admin.stream.data.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

@Slf4j
public class FastJsonSerializer<T> implements Serializer<T> {


    private final Class<T> type;

    public FastJsonSerializer(Class<T> type) {
        this.type = type;
    }

    private static SerializeConfig config = SerializeConfig.getGlobalInstance();
    static {
        config.put(Double.class, new DoubleSerializer("#.######"));
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        return JSON.toJSONBytes(data, config, SerializerFeature.DisableCircularReferenceDetect);
    }
}
