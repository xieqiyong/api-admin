package com.hz.api.admin.engine.handler;

import com.github.benmanes.caffeine.cache.Cache;
import com.hz.api.admin.engine.SocketServer;
import com.hz.api.admin.model.message.ClientConfigMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author xieqiyong66@gmail.com
 * @description: SocketHandler
 * @date 2022/7/9 9:51 下午
 */
public class SocketHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);

    private final ChannelGroup channels;

    private final SocketServer server;

    public SocketHandler(SocketServer server, ChannelGroup channels){
        this.channels = channels;
        this.server = server;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        log.info("收到消息: {}", msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("测试");
        this.channels.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端离线：" + ctx.channel().id().asShortText());
        this.channels.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.channel().close();
        this.channels.remove(ctx.channel());
    }
}
