package com.bsoft.deploy.controller;

import com.bsoft.deploy.MasterApplication;
import com.bsoft.deploy.bean.FileWalkerFactory;
import com.bsoft.deploy.file.FileWalker;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.service.AppFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc
 * Created on 2018/8/7.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/file")
public class FileController {
    private final static Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private AppFileService fileService;

    @RequestMapping(value = {"/fileList"}, method = RequestMethod.GET)
    public HttpResult fileList(@RequestParam int appId, @RequestParam String path) {
        FileWalkerFactory factory = MasterApplication.get().getBean(FileWalkerFactory.class);
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

    @RequestMapping(value = {"/fileProp"}, method = RequestMethod.GET)
    public HttpResult fileProp(@RequestParam int appId) {
        FileWalkerFactory factory = MasterApplication.get().getBean(FileWalkerFactory.class);
        FileWalker fileWalker = factory.getInstance(appId);
        HashMap<String, Object> prop = new HashMap<>(2);
        prop.put("appPath", fileWalker.getAppPath(appId));
        return new HttpResult(prop);
    }

    /**
     * 应用文件状态同步
     *
     * @param appId
     * @return
     */
    @RequestMapping(value = "sync", method = RequestMethod.GET)
    public HttpResult syncApp(@RequestParam int appId) {
        FileWalkerFactory factory = MasterApplication.get().getBean(FileWalkerFactory.class);
        FileWalker fileWalker = factory.getInstance(appId);
        fileWalker.syncFiles(appId);
        return new HttpResult();
    }

    @RequestMapping(value = "apps", method = RequestMethod.GET)
    public HttpResult loadApps() {
        return new HttpResult(fileService.loadApps());
    }
}
