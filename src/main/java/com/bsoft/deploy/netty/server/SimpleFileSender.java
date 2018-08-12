package com.bsoft.deploy.netty.server;

import com.bsoft.deploy.dao.entity.AppFile;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;

import java.util.List;

/**
 * 更新文件分发
 * Created on 2018/8/10.
 *
 * @author yangl
 */
public class SimpleFileSender {

    /**
     * 发送单个文件到所有节点
     * @param file
     */
    public static void handOut(AppFile file) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for(Channel ch : group) {
            ch.writeAndFlush(file);
        }
    }

    /**
     * 发送单个文件到指定节点
     * @param file
     * @param targetIp
     */
    public static void handOut(AppFile file, String targetIp) {
        ChannelGroup group = SimpleFileServerHandler.channels;

    }

    /**
     * 发送文件列表
     * @param files
     * @param targetIp
     */
    public static void handOut(List<AppFile> files, String targetIp) {

    }

    /**
     * 发送文件列表到目标节点群
     * @param files
     * @param targetIps
     */
    public static void handOut(List<AppFile> files, List<String> targetIps) {

    }


}
