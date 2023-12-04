package com.hz.api.admin.stream.transition;

import org.apache.kafka.streams.kstream.TimeWindows;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 北海
 * @description 用户聚合窗口大小枚举
 * @date 2023-04-25 10:47
 */
public interface Mesh {

    long session = -1L;
    long sessionSnapshot = 0L;
    //1秒
    long s1 = 1000L;
    //5秒
    long s5 = 5000L;
    //10秒
    long s10 = 10000L;
    //30秒
    long s30 = 30000L;
    //60秒
    long s60 = 60000L;
    //300秒
    long s300 = 300000L;

    TimeWindows window_1s = TimeWindows.of(Duration.ofSeconds(1)).advanceBy(Duration.ofSeconds(1)).grace(Duration.ofSeconds(10));
    TimeWindows window_5s = TimeWindows.of(Duration.ofSeconds(5)).advanceBy(Duration.ofSeconds(5)).grace(Duration.ofSeconds(10));
    TimeWindows window_10s = TimeWindows.of(Duration.ofSeconds(10)).advanceBy(Duration.ofSeconds(10)).grace(Duration.ofSeconds(10));
    TimeWindows window_30s = TimeWindows.of(Duration.ofSeconds(30)).advanceBy(Duration.ofSeconds(30)).grace(Duration.ofSeconds(10));
    TimeWindows window_60s = TimeWindows.of(Duration.ofSeconds(60)).advanceBy(Duration.ofSeconds(60)).grace(Duration.ofSeconds(10));
    TimeWindows window_300s = TimeWindows.of(Duration.ofSeconds(300)).advanceBy(Duration.ofSeconds(300)).grace(Duration.ofSeconds(10));

    List<TimeWindows> agentWindows = Stream.of(window_5s, window_10s, window_30s, window_60s, window_300s).collect(Collectors.toList());

}
