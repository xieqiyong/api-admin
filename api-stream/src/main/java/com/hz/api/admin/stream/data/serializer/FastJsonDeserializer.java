package com.hz.api.admin.stream.data.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * @author 北海
 * @description fastjson反序列化器
 * @date 2023-03-27 19:13
 */
@Slf4j
public class FastJsonDeserializer<T> implements Deserializer<T> {

    private final Class<T> type;

    public FastJsonDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return JSON.parseObject(data, type, Feature.DisableCircularReferenceDetect);
    }
}
