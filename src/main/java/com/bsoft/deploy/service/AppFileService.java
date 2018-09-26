package com.bsoft.deploy.service;

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



    public List<FileDTO> loadAppFile(int appId, String path) {
        return fileMapper.loadAppFile(appId, path);
    }

    public List<FileDTO> loadAppIgnoreFiles(int appId) {
        return fileMapper.loadAppFilesWithSign(appId, 1);
    }

    public FileDTO loadAppIgnoreFile(int appId,String path) {
        return fileMapper.loadAppFileWithSign(appId,path, 1);
    }



    public void saveAppFile(FileDTO fileDTO) {
        fileMapper.saveAppFile(fileDTO);
    }

    public int updateAppFile(FileDTO fileDTO) {
        return fileMapper.updateAppFile(fileDTO);
    }

}
