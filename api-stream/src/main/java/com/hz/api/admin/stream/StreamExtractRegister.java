package com.hz.api.admin.stream;

import com.hz.api.admin.stream.config.CustomTimestampExtractor;
import com.hz.api.admin.stream.config.RocksConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class StreamExtractRegister {

    private static final Map<String, StreamsBuilder> streamsBuilderMap = new ConcurrentHashMap<>(64);

    private static final Map<String, Properties> registerPropertiesMap = new ConcurrentHashMap<>(64);
    /**
     * 注册数据流
     * @param applicationId 应用ID 同一个应用ID可以多进程任务分算子执行
     * @param topic 要监听的数据流topic
     * @param properties 自定义参数，可以为空，为空则使用默认参数
     * @return 数据流
     */
    public static StreamsBuilder register(String serverAddress, String topic, String applicationId, Serde serde, int streamThreads){
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        Properties properties = new Properties();
        //配置kafka连接端口
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);
        //配置消息只消费一次（默认最少一次)
        //properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_BETA);
        //配置数据默认序列化方式方式
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, serde.getClass());
        //应用id配置 topic前缀
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        //最大副本配置
        //properties.put(StreamsConfig.MAX_WARMUP_REPLICAS_CONFIG, 1);
        //复制因子配置
        properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        properties.put("max.request.size", "10485760");
        properties.put("auto.offset.reset", "latest");
        properties.put("message.max.bytes", "10485760");
        //设置state存储地址
        properties.put(StreamsConfig.STATE_DIR_CONFIG, "/Users/liusu/Documents/docker/kafka");
        //每次拉取消息数
        properties.put("max.poll.records", "2000");
        //并行计算线程数
        properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, streamThreads);
        //properties.put(StreamsConfig.BUFFERED_RECORDS_PER_PARTITION_CONFIG, 5);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        //自动关闭空闲连接时长
        //properties.put(StreamsConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, -1);
        //rocksDB配置
        properties.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, RocksConfig.class.getName());
        properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, CustomTimestampExtractor.class.getName());
        //配置用于时间窗口的时间提取器
        registerPropertiesMap.put(applicationId + "_" + topic, properties);
        streamsBuilderMap.put(applicationId + "_" + topic, streamsBuilder);
        return streamsBuilder;
    }

    public static void start() {
        try {
            streamsBuilderMap.forEach((key, value) -> {
                Properties properties = registerPropertiesMap.get(key);
                KafkaStreams streams = new KafkaStreams(value.build(), properties);
                streams.start();
            });
        }catch (Exception e){
            log.error("启动异常: {}", e);
        }

    }

}
