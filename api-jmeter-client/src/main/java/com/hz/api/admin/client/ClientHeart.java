package com.hz.api.admin.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientHeart {

    private static Object LOCK = new Object();

    private static boolean started = false;

    public static Channel channel = null;

    private static ClientConnection connection;

    /**
     * PerfMa扩展加载
     */
    public static void start() {
        if (!started) {
            synchronized (LOCK) {
                if (!started) {
                    started = true;
                    start0();
                }
            }
        }
    }

    private static boolean start0(){
        try {
            connection = new ClientConnection();
            channel = connection.registerServer();
            log.info("压测引擎通过xshark控制启动");
        } catch (Exception e) {
            log.error("建立xshark通道异常，压测引擎自杀");
            System.exit(-1);
        }

        // 启动XOwl心跳
        log.debug("开始向xshark发送心跳");
        checkTaskStatus();
        return true;
    }

    private static void checkTaskStatus() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isStarted()) {
                    try {
                        Thread.sleep(1);
                        if (ClientHeart.channel != null && ClientHeart.channel.isActive()) {
                            String msg = "PING:222\n";
                            ClientHeart.channel.writeAndFlush(msg);
                            log.debug("向xshark发送心跳：" + msg);
                        }else{
                            log.error("建立xshark通道断开，压测引擎自杀");
                            System.exit(-1);
                        }
                    } catch (InterruptedException i) {
                        log.debug("xshark心跳线程被中断", i);
                    } catch (Exception e) {
                        log.error("向xshark发送心跳异常，压测引擎自杀");
                        System.exit(-1);
                    }
                }
            }
        }, "HeartbeatChecker");
        t.setDaemon(true);
        t.start();
    }

    public static boolean isStarted() {
        return started;
    }



    public static void write(Object data){
        channel.write(JSON.toJSONString(data));
    }
}
