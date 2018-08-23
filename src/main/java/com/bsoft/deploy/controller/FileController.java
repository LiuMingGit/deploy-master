package com.bsoft.deploy.controller;

import com.bsoft.deploy.bean.FileWalkerFactory;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.file.FileWalker;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.service.AppFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private AppFileService fileService;

    /**
     * 获取应用文件列表(文件系统)
     *
     * @param appId
     * @param path
     * @return
     */
    @RequestMapping(value = {"/fileList"}, method = RequestMethod.GET)
    public HttpResult fileList(@RequestParam int appId, @RequestParam String path) {
        FileWalkerFactory factory = Global.getAppContext().getBean(FileWalkerFactory.class);
        FileWalker fileWalker = factory.getInstance(appId);

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
    public HttpResult fileProp(@RequestParam int appId) {
        HashMap<String, Object> prop = new HashMap<>(2);
        prop.put("appPath", Global.getAppStore().getApp(appId).getPath());
        return new HttpResult(prop);
    }

    /**
     * 应用文件状态同步
     * 一般用于应用初始化
     *
     * @param appId
     * @return
     */
    @RequestMapping(value = "sync", method = RequestMethod.GET)
    public HttpResult syncApp(@RequestParam int appId) {
        FileWalkerFactory factory = Global.getAppContext().getBean(FileWalkerFactory.class);
        FileWalker fileWalker = factory.getInstance(appId);
        fileWalker.syncFiles(appId);
        return new HttpResult();
    }

    /**
     * 将应用文件同步到所有的目标节点
     *
     * @param appId 应用id
     * @return 状态
     */
    @RequestMapping(value = "syncToSlave", method = RequestMethod.GET)
    public HttpResult syncFileToSlave(@RequestParam int appId) {
        FileWalkerFactory factory = Global.getAppContext().getBean(FileWalkerFactory.class);
        FileWalker fileWalker = factory.getInstance(appId);
        fileWalker.syncFilesToSlave(appId);
        return new HttpResult();
    }

}
