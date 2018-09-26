package com.bsoft.deploy.file;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.*;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.utils.FileUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * 文件同步任务
 * Created on 2018/8/15.
 *
 * @author yangl
 */
public class FileWorker extends RecursiveAction {
    private static final Logger logger = LoggerFactory.getLogger(FileWorker.class);
    private static final int THRESHOLD = 50;
    /**
     * 需要同步的目标节点
     */
    private Guard guard;
    /**
     * 子节点
     */
    private Channel channel;
    /**
     * 传输的文件列表
     */
    private List<FileDTO> files;

    public FileWorker(Guard guard, Channel channel, List<FileDTO> files) {
        this.guard = guard;
        this.channel = channel;
        this.files = files;
    }

    @Override
    public void completeExceptionally(Throwable ex) {
        logger.error("send file error!" + ex.getMessage());
    }

    @Override
    public void compute() {
        if (files.size() < THRESHOLD) {
            // 文件同步
            for (FileDTO file : files) {
                try {
                    // 判断本地文件是否存在
                    if (!FileUtils.exists(file.getPath())) {
                        logger.error("文件同步失败!");
                        continue;
                    }
                    // 插入文件发送记录
                    FileLog log = new FileLog();
                    setSlave(log, channel);
                    int logId = saveLog(file, log);
                    String basePath = Global.getAppStore().getApp(file.getAppId()).getPath()
                            + File.separator + "version_" + file.getPkgId() + File.separator;
                    basePath = FileUtils.pathFormat(basePath);
                    int slaveAppId = Global.getSlaveStore().getSlaveApp(guard.getSlaveId(), file.getAppId()).getId();
                    AppFile f = new AppFile(file.getPath(), basePath);
                    f.setLogId(logId);
                    f.setId(file.getId());
                    f.setMark(file.getMark());
                    f.setSlaveAppId(slaveAppId);
                    f.setUpdateId(guard.getUpdateId());
                    channel.writeAndFlush(f);
                    guard.setSendNum(1);
                } catch (Exception e) {
                    logger.error("文件{}同步失败!", file.getFilename(), e);
                    FileLog log = new FileLog();
                    log.setStatus(-1);
                    log.setMessage(e.getMessage().substring(0, 300));
                    setSlave(log, channel);
                    saveLog(file, log);
                }
            }
        } else {
            //拆分任务
            // System.out.println("任务分解:"+files.size());
            int middle = files.size() / 2;
            FileWorker leftTask = new FileWorker(guard, channel, files.subList(0, middle));
            FileWorker rightTask = new FileWorker(guard, channel, files.subList(middle, files.size()));
            leftTask.fork();
            rightTask.fork();
        }
    }

    private void setSlave(FileLog log, Channel ch) {
        String ip = getChannelIp(ch);
        Slave slave = Global.getSlaveStore().getSlave(ip);
        log.setSlaveId(slave.getId());
    }

    private String getChannelIp(Channel ch) {
        InetSocketAddress socket = (InetSocketAddress) ch.remoteAddress();
        return socket.getHostName();
    }

    public static int saveLog(FileDTO file, FileLog log) {
        // System.out.println(log.getMark());
        AppFileMapper fileMapper = Global.getAppContext().getBean(AppFileMapper.class);
        log.setAppId(file.getAppId());
        log.setFileId(file.getId());
        log.setPkgId(file.getPkgId());
        log.setMark(file.getMark());
        log.setOptime(new Date());
        fileMapper.saveFileTransferLog(log);
        return log.getId();
    }

}
