package com.bsoft.deploy.netty.client;

import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.file.FileWoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 子节点接收文件传输列表
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        try {
            AppFile file = (AppFile) o;
            FileWoker fw = new FileWoker();
            fw.receive(file);
            System.out.println(file.getName() + " 同步成功");
        } catch (Exception e) {
            logger.error("文件同步失败!", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("服务连接异常,连接关闭!");
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("连接已失效,尝试重新连接...");
        //使用过程中断线重连

        super.channelInactive(ctx);
    }
}
