package com.hz.api.admin.client;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * 客户端程序用于压力机通信
 */
@Slf4j
public class ClientMain {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 启动客户端
        ClientHeart.start();
    }
}