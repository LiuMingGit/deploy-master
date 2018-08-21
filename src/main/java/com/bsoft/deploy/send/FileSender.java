package com.bsoft.deploy.send;

import com.bsoft.deploy.dao.entity.FileDTO;
import com.bsoft.deploy.file.FileWorker;
import com.bsoft.deploy.netty.server.SimpleFileServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.List;

/**
 * 更新文件分发
 * Created on 2018/8/10.
 *
 * @author yangl
 */
public class FileSender {

    /**
     * 发送单个文件到所有节点
     *
     * @param file
     */
    public static void handOut(FileDTO file) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            // @todo 记录同步日志
            ch.writeAndFlush(file);
        }
    }

    /**
     * 发送单个文件到指定节点
     *
     * @param file
     * @param targetIp
     */
    public static void handOut(FileDTO file, String targetIp) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            if (ch.remoteAddress().toString().contains(targetIp)) {
                // @todo 记录同步日志
                ch.writeAndFlush(file);
            }
        }
    }

    /**
     * 发送文件列表
     *
     * @param files
     * @param targetIp
     */
    public static void handOut(List<FileDTO> files, String targetIp) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            if (ch.remoteAddress().toString().contains(targetIp)) {
                new FileWorker(ch, files).compute();
            }
        }
    }

    /**
     * 发送文件列表到目标节点群
     *
     * @param files
     * @param targetIps
     */
    public static void handOut(List<FileDTO> files, List<String> targetIps) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            for (String targetIp : targetIps) {
                if (ch.remoteAddress().toString().contains(targetIp)) {
                    new FileWorker(ch, files).compute();
                }
            }
        }
    }

    /**
     * 发送文件列表至所有节点
     *
     * @param files
     */
    public static void handOut(List<FileDTO> files) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            new FileWorker(ch, files).compute();
        }
    }


}
