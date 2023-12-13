package com.hz.api.admin.client;
import com.hz.api.admin.client.client.message.Message;
import com.hz.api.admin.client.server.ServerDataListener;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

/**
 * 客户端程序用于压力机通信
 */
@Slf4j
public class ClientMain {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 启动客户端
        Configuration configuration = new Configuration();
        configuration.setServerAddress("172.16.0.109:15001");
        configuration.setClientId("172.16.0.109@2020032000");
        DataServerClient client = new DataServerClient(configuration);
        client.start();
        // 客户端发送消息
        Message m = new Message();
        m.setTopic("dasdsadasda");
        m.setBody(new String("123124").getBytes(StandardCharsets.UTF_8));
        client.getMessageManager().send(m);
        // 注册监听器来接收服务端的数据
        client.getAutoMessageManager().registerServerDataListener(new ServerDataListener() {
            @Override
            public void onData(byte[] value) {
                //  这里接收服务端发来的数据
            }
        });
    }
}