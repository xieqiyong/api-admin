package com.hz.api.admin.stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.hz.api.admin.stream.data.enums.EsIndexEnum;
import com.hz.api.admin.stream.write.WriteEsManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Component
@Slf4j
public class DataProcess implements Runnable, InitializingBean, Closeable {

    @Value("${kafka.address}")
    private String kafkaServer;
    @Value("${kafka.group-id}")
    private String groupId;
    private ExecutorService executors;
    private ScheduledExecutorService scheduler;
    private KafkaConsumer<byte[],byte[]> consumer;
    private AtomicBoolean running = new AtomicBoolean(true);
    private ConcurrentMap<TopicPartition, OffsetAndMetadata> offsetsCollector = new ConcurrentHashMap<>();
    @Autowired
    private List<WriteEsManager> writeEsManagerList;

    @Override
    public void close() throws IOException {
        running.set(false);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // starting ...
        this.consumer = createConsumer(kafkaServer, groupId);
        new Thread(this, "data-processor")
                .start();
        log.info("data-processor started.");
    }

    @Override
    public void run() {
        if(ObjectUtils.isEmpty(consumer)){
            return;
        }
        consumer.subscribe(EsIndexEnum.kafkaIndex());
        while (running.get()) {
            try {
                commitOffsets();
                ConsumerRecords<byte[],byte[]> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<byte[], byte[]> record : records) {
                    TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                    OffsetAndMetadata offset = new OffsetAndMetadata(record.offset());
                    writeRequestToEs(record.topic(), record.value(), record.timestamp());
                    offsetsCollector.put(tp, offset);
                }
            } catch (Exception e) {
                log.warn("预处理数据时发生异常", e);
            }
        }
        commitOffsets();
        consumer.close();
        log.info("data-processor exited.");
    }
    private void commitOffsets() {
        if (!offsetsCollector.isEmpty()) {
            Map<TopicPartition, OffsetAndMetadata> tmp = new HashMap<>(offsetsCollector);
            offsetsCollector.clear();
            consumer.commitAsync(tmp, null);
            log.info("committed offsets: {}", tmp);
        }
    }

    public void writeRequestToEs(String topic, byte[] value, long timestamp){
        writeEsManagerList.forEach(writeEsManager -> {
            if(writeEsManager.topic().equals(topic)){
                String v = new String(value, StandardCharsets.UTF_8);
                writeEsManager.writeEs(v, timestamp);
            }
        });

    }

    private KafkaConsumer<byte[],byte[]> createConsumer(String kafkaServer, String groupId) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", Objects.requireNonNull(kafkaServer));
        props.setProperty("group.id", Objects.requireNonNull(groupId));
        props.setProperty("client.id", UUID.randomUUID().toString().substring(0, 13));
        props.setProperty("enable.auto.commit", "false"); // 重要！禁止 offset 自动提交
        props.setProperty("retries", "5");
        props.setProperty("fetch.min.bytes", "10240000");
        props.setProperty("max.poll.records", "10000");
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return new KafkaConsumer<>(props);
    }
}
