package com.bsoft.deploy.controller;

import com.alibaba.fastjson.JSON;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.FileDTO;
import com.bsoft.deploy.file.FileWalker;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.service.AppFileService;
import com.bsoft.deploy.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件处理 restful api
 * Created on 2018/8/7.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    AppFileService appFileService;

    /**
     * 获取应用文件列表(文件系统)
     *
     * @param path
     * @return
     */
    @RequestMapping(value = {"/fileList"}, method = RequestMethod.GET)
    public HttpResult fileList(@RequestParam String path) {
        // FileWalkerFactory factory = Global.getAppContext().getBean(FileWalkerFactory.class);
        FileWalker fileWalker = Global.getAppContext().getBean(FileWalker.class);
        path = FileUtils.pathFormat(path);
        List<Map<String, Object>> fileTree = fileWalker.getFileTree(path);
        if (fileTree.size() == 1) {
            Map<String, Object> father = fileTree.get(0);
            next(fileWalker, father, father);
        }
        return new HttpResult(fileTree);
    }

    private void next(FileWalker fileWalker, Map<String, Object> father, Map<String, Object> node) {
        if (!(boolean) node.get("leaf")) {
            List<Map<String, Object>> nextLevelNodes = fileWalker.getFileTree((String) node.get("path"));
            if (nextLevelNodes.size() == 1) {
                Map<String, Object> nextNode = nextLevelNodes.get(0);
                father.put("name", father.get("name") + "." + nextNode.get("name"));
                father.put("path", nextNode.get("path"));
                next(fileWalker, father, nextNode);
            } else {
                father.put("children", nextLevelNodes);
            }

        }
    }

    /**
     * 获取应用相关参数
     *
     * @param appId
     * @return
     */
    @RequestMapping(value = {"/fileProp"}, method = RequestMethod.GET)
    public HttpResult fileProp(@RequestParam int appId, @RequestParam int pkgId) {
        HashMap<String, Object> prop = new HashMap<>(2);
        String path = FileUtils.pathFormat(Global.getAppStore().getApp(appId).getPath() + File.separator + "version_" + pkgId + File.separator);
        FileWalker fw = Global.getAppContext().getBean(FileWalker.class);
        prop.put("appPath", path);
        prop.put("appFileCount", fw.getFilesCount(new File(path)));
        return new HttpResult(prop);
    }


    /**
     * 更新忽略
     *
     * @param jsonData
     * @return
     */
    @RequestMapping(value = {"/ignore"}, method = RequestMethod.POST)
    public HttpResult saveApp(@RequestBody String jsonData) {
        HashMap data = JSON.parseObject(jsonData, HashMap.class);
        int appId = (int) data.get("appId");
        String path = (String) data.get("path");
        String fileName = (String) data.get("name");
        FileDTO file = appFileService.loadAppIgnoreFile(appId, path);
        if (file == null) {
            file = new FileDTO();
            file.setAppId(appId);
            file.setFilename(fileName);
            file.setPath(path);
            file.setSign(1);
            file.setOptime(new Date());
            appFileService.saveAppFile(file);
        }
        return new HttpResult();
    }


    /**
     * 更新忽略的文件列表
     *
     * @param appId
     * @return
     */
    @RequestMapping(value = {"/ignore/list"}, method = RequestMethod.GET)
    public HttpResult ignoreList(int appId) {
        List<FileDTO> file = appFileService.loadAppIgnoreFiles(appId);
        return new HttpResult(file);
    }

    /**
     * 删除更新忽略的文件
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/ignore/remove"}, method = RequestMethod.GET)
    public HttpResult removeIgnoreFile(int id) {
        appFileService.removeAppIgnoreFile(id);
        return new HttpResult();
    }
}
