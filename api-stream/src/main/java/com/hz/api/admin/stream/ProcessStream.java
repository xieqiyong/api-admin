package com.hz.api.admin.stream;

import com.hz.api.admin.stream.transition.Transition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.streams.StreamsBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Properties;

@Configuration
@Slf4j
public class ProcessStream implements ApplicationRunner {

    @Resource
    private List<Transition> dataTransitions;

    @Value("${kafka.address}")
    private String kafkaServer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        //注册数据转换
        if (CollectionUtils.isNotEmpty(dataTransitions)) {
            dataTransitions.forEach(dataTransition -> {
                if (!dataTransition.getClass().isInterface() && !Modifier.isAbstract(dataTransition.getClass().getModifiers())) {
                    // 注册数据抽取
                    StreamsBuilder register = StreamExtractRegister.register(kafkaServer, dataTransition.topic(), dataTransition.appId(), dataTransition.getSerde(), dataTransition.streamThreads());
                    dataTransition.transition(register);
                }
            });
            StreamExtractRegister.start();
            log.info("启动kafka-stream聚合success...");
        }
    }
}