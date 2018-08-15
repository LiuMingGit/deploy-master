package com.bsoft.deploy.service;

import com.bsoft.deploy.dao.entity.App;
import com.bsoft.deploy.dao.entity.FileDTO;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文件操作service
 * Created on 2018/8/13.
 *
 * @author yangl
 */
@Service
public class AppFileService {
    @Autowired
    AppFileMapper fileMapper;

    public FileDTO loadAppFile(int appId, String path) {
        return fileMapper.loadAppFile(appId, path);
    }

    public void saveAppFile(FileDTO fileDTO) {
        fileMapper.saveAppFile(fileDTO);
    }

    public int updateAppFile(FileDTO fileDTO) {
        return fileMapper.updateAppFile(fileDTO);
    }

    public String findAppPath(int appId) {
        return fileMapper.findPathById(appId);
    }

    public List<App> loadApps() {
        return fileMapper.loadApps();
    }
}
