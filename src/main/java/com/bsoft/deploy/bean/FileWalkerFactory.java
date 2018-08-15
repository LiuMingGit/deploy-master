package com.bsoft.deploy.bean;

import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.file.FileWalker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ConcurrentHashMap;

/**
 * desc
 * Created on 2018/8/14.
 *
 * @author yangl
 */
public class FileWalkerFactory {
    private ConcurrentHashMap<Integer, FileWalker> instances = new ConcurrentHashMap<>();

    @Autowired
    AppFileMapper fileMapper;


    public FileWalker getInstance(int appId) {
        if (instances.contains(appId)) {
            return instances.get(appId);
        }
        FileWalker fw = new FileWalker();
        String appPath = fileMapper.findPathById(appId);
        fw.setAppPath(appPath);
        fw.setFileMapper(fileMapper);
        fw.init();
        instances.put(appId, fw);
        return fw;
    }

}
