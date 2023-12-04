package com.hz.api.admin.stream.transition;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.StreamsBuilder;

public interface Transition<T> {

    /**
     * 要进行聚合的原始数据topic
     * @return
     */
    String topic();

    String appId();

    /**
     * 元数据序列化器
     * @return
     */
    public Serde<T> getSerde();

    /**
     * 聚合转换实现
     */
    void transition(StreamsBuilder streamsBuilder);

    /**
     * 并行计算线程数
     * @return
     */
    int streamThreads();

}
