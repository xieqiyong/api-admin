package com.hz.api.admin.client;

import com.alibaba.fastjson.JSON;
import com.hz.api.admin.client.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class ClientConnection {

    private ChannelFuture future = null;

    public Channel registerServer(){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 添加对byte数组的编解码，netty提供了很多编解码器，你们可以根据需要选择
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new LineBasedFrameDecoder(1024*10));
                            // 添加上自己的处理器
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            future = bootstrap.connect("localhost", 17951).channel().closeFuture().sync();
            return future.channel();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
        return null;
    }
}
