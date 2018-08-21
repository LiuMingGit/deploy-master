package com.bsoft.deploy.file;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.dao.entity.FileDTO;
import com.bsoft.deploy.dao.entity.FileLog;
import com.bsoft.deploy.dao.entity.Slave;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.utils.FileUtils;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * 子节点
     */
    private Channel channel;
    /**
     * 传输的文件列表
     */
    private List<FileDTO> files;

    public FileWorker(Channel channel, List<FileDTO> files) {
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
                    AppFile f = new AppFile(file.getPath(),
                            Global.getAppBasePath(file.getAppId()));
                    f.setLogId(logId);
                    f.setId(file.getId());
                    f.setMark(file.getMark());
                    f.setAppId(file.getAppId());
                    channel.writeAndFlush(f);

                } catch (Exception e) {
                    logger.error("文件{}同步失败!", file.getFilename(), e);
                    FileLog log = new FileLog();
                    log.setStatus(-1);
                    log.setMessage(e.getMessage());
                    setSlave(log, channel);
                    saveLog(file, log);
                }
            }
        } else {
            //拆分任务
            int middle = files.size() / 2;
            FileWorker leftTask = new FileWorker(channel, files.subList(0, middle));
            FileWorker rightTask = new FileWorker(channel, files.subList(middle + 1, files.size() - 1));
            leftTask.fork();
            rightTask.fork();
        }
    }

    private void setSlave(FileLog log, Channel ch) {
        String ip = getChannelIp(ch);
        Slave slave = Global.getSlave(ip);
        log.setSlaveId(slave.getId());
    }

    private String getChannelIp(Channel ch) {
        String address = ch.remoteAddress().toString();
        return address.substring(1, address.indexOf(":"));
    }

    public static int saveLog(FileDTO file, FileLog log) {
        AppFileMapper fileMapper = Global.getFileMapper();
        log.setAppId(file.getAppId());
        log.setFileId(file.getId());
        log.setMark(file.getMark());
        log.setOptime(new Date());
        int flag = fileMapper.saveFileTransferLog(log);
        System.out.println(flag + ":" +log.getId());
        return log.getId();
    }

}
