package com.hz.api.admin.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@Slf4j
public class EngineManager implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        SocketServer socketServer = new SocketServer();
        socketServer.start();
        log.info("心跳检测启动成功");
    }
}