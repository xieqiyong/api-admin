package com.hz.api.admin.stream.config;

import com.alibaba.fastjson.JSON;
import com.hz.api.admin.stream.data.model.metric.RequestMetric;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

@Slf4j
public class CustomTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        try {
//            RequestMetric requestMetric = JSON.parseObject(JSON.toJSONString(record.value()), RequestMetric.class);
//            // 使用 "start" 字段作为事件时间戳
//            long timestamp = requestMetric.getStartTime();
//            return timestamp;
            return record.timestamp();
        } catch (Exception e) {
            // 处理异常
            e.printStackTrace();
            return 0L; // 返回默认的时间戳
        }
    }
}
