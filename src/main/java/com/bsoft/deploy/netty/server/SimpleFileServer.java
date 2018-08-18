package com.bsoft.deploy.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * 基于netty的server端实现
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileServer {
    private final static Logger logger = LoggerFactory.getLogger(SimpleFileServer.class);
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    @Value("${netty.server.port}")
    private int port;

    public SimpleFileServer() {
    }

    public SimpleFileServer(int port) {
        this.port = port;
    }


    public void start() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SimpleFileServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            logger.info("文件传输server端启动了");
            // 绑定端口，开始接收进来的连接
            b.bind(port).sync();

        } catch (InterruptedException e) {
            logger.error("file server start fails", e);
        }
    }

}
