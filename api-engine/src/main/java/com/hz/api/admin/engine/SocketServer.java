package com.hz.api.admin.engine;

import com.hz.api.admin.engine.handler.SocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author xieqiyong66@gmail.com
 * @description: SocketServer
 * @date 2022/7/10 4:04 下午
 */
public class SocketServer {

    @Getter
    private ServerBootstrap serverBootstrap;


    private static final int DATA_MAX_LENGTH = 1024 * 10;

    private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private Channel channel;

    private ChannelGroup channels;

    private String host = "0.0.0.0";
    private Integer port;

    /**
     * 启动netty服务器
     */
    public void start() {
        this.init();
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast(new ReadTimeoutHandler(5000, TimeUnit.MINUTES));
                // 添加对byte数组的编解码，netty提供了很多编解码器，你们可以根据需要选择
                pipeline.addLast(new StringEncoder());
                pipeline.addLast(new LineBasedFrameDecoder(DATA_MAX_LENGTH));
                // 添加上自己的处理器
                pipeline.addLast(new SocketHandler(SocketServer.this, channels));
            }
        });
        ChannelFuture future = this.serverBootstrap.bind(new InetSocketAddress(host, 17951));
        future.syncUninterruptibly();
        channel = future.channel();
        log.info("Netty started on port: {} (TCP) with boss thread {}", 17951, 1);
    }

    /**
     * 初始化netty配置
     */
    private void init() {
        // 创建两个线程组，bossGroup为接收请求的线程组，一般1-2个就行
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 实际工作的线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        this.serverBootstrap = new ServerBootstrap();
        this.serverBootstrap.group(bossGroup, workerGroup);
        this.serverBootstrap.channel(NioServerSocketChannel.class);
        this.serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        this.serverBootstrap.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
        this.serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE);
    }
}
