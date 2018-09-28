package com.bsoft.deploy.service;

import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.FileDTO;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.send.CmdSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        reloadCache(fileDTO.getAppId());
    }

    public int updateAppFile(FileDTO fileDTO) {
        return fileMapper.updateAppFile(fileDTO);
    }

    public void removeAppIgnoreFile(int id) {
        fileMapper.deleteById(id);
    }

    public void reloadCache(int appId) {
        // master 主节点
        Global.getAppStore().reloadFiles(appId);
        // slave 子节点
        Order order = new Order();
        order.setType(Constant.CMD_RELOAD_CACHE);
        Map<String,Object> req = new HashMap<>();
        req.put("target","appFile");
        // 0 表示全部
        req.put("id",appId);
        order.setReqData(req);
        CmdSender.handOut(order);
    }
}
