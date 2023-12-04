package com.hz.api.admin.stream.config;

import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;

import java.util.Map;

public class RocksConfig implements RocksDBConfigSetter {
    @Override
    public void setConfig(String s, Options options, Map<String, Object> map) {
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
        //写缓冲区大小
        //options.setWriteBufferSize(256 * 1024 * 1024L);
        //options.setMaxOpenFiles(1000);
        //设置state最大存储大小30G,该数据主要用于异常离线后的数据恢复
        options.setMaxTableFilesSizeFIFO(30 * 1024 * 1024 * 1024);

        //设置 RocksDB 的并行线程数 处理合并和压缩
        //options.setIncreaseParallelism(6);
        //设置 RocksDB 后台压缩线程数的最大值。默认值为 1
        //options.setMaxBackgroundCompactions(2);
        //设置 RocksDB 后台 flush 线程数的最大值。默认值为 1
        options.setMaxBackgroundFlushes(2);
    }

    @Override
    public void close(String storeName, Options options) {
        RocksDBConfigSetter.super.close(storeName, options);
    }
}
