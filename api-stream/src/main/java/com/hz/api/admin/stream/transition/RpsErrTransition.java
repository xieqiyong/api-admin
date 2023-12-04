package com.hz.api.admin.stream.transition;

import com.hz.api.admin.stream.data.enums.EsIndexEnum;
import com.hz.api.admin.stream.data.model.metric.RequestMetric;
import com.hz.api.admin.stream.data.model.metric.RpsMetric;
import com.hz.api.admin.stream.data.serde.RequestMetricSerde;
import com.hz.api.admin.stream.data.serde.RpsMetricSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RpsErrTransition implements Transition<RequestMetric>{
    @Override
    public String topic() {
        return EsIndexEnum.RPS_FAIL.getTopic();
    }

    @Override
    public String appId() {
        return "2023-RPS-ERR";
    }

    @Override
    public Serde getSerde() {
        return Serdes.Bytes();
    }


    public Serde getFinalSerde() {
        return Serdes.Bytes();
    }

    private Produced<String, RpsMetric> targetProduced = Produced.with(Serdes.String(), new RpsMetricSerde());



    @Override
    public void transition(StreamsBuilder streamsBuilder) {
        KStream<String, RequestMetric> stream = streamsBuilder.stream(this.topic(),Consumed.with(Serdes.String(), new RequestMetricSerde()));
        // 使用1秒的滚动窗口进行窗口化聚合
        stream.map((key, value) -> {
                    RpsMetric rpsMetric = new RpsMetric();
                    BeanUtils.copyProperties(value, rpsMetric);
                    rpsMetric.setErrCount(1L);
                    return KeyValue.pair(value.getUrlAsString(), rpsMetric);
                })
                .groupByKey(Grouped.with(Serdes.String(), new RpsMetricSerde()))
                .windowedBy(Mesh.window_1s)
                .reduce(
                        (aggValue, newValue) -> {
                            return aggValue.addErr(newValue);
                        }
                )
                .toStream()
                .map((key, value) -> {
                    String newKey = key.key() + "_" + key.window().start() + "_" + key.window().end();
                    value.setKey(newKey);
                    return KeyValue.pair(newKey, value);
                })
                .to(EsIndexEnum.RPS_FAIL.getIndex(), Produced.with(Serdes.String(), new RpsMetricSerde()));

    }


    @Override
    public int streamThreads() {
        return 8;
    }
}
