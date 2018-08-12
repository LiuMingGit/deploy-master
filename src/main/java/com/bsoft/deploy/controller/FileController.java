package com.bsoft.deploy.controller;

import com.bsoft.deploy.file.FileWalker;
import com.bsoft.deploy.http.HttpResult;
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
    private FileWalker fileWalker;

    @RequestMapping(value = {"/fileList"}, method = RequestMethod.GET)
    public HttpResult fileList(@RequestParam String path) {
        List<Map<String, Object>> fileTree = fileWalker.getFileTree(path);
        if (fileTree.size() == 1) {
            Map<String, Object> father = fileTree.get(0);
            next(father, father);
        }
        return new HttpResult(fileTree);
    }

    private void next(Map<String, Object> father, Map<String, Object> node) {
        if (!(boolean) node.get("leaf")) {
            List<Map<String, Object>> nextLevelNodes = fileWalker.getFileTree((String) node.get("path"));
            if (nextLevelNodes.size() == 1) {
                Map<String, Object> nextNode = nextLevelNodes.get(0);
                father.put("name", father.get("name") + "." + nextNode.get("name"));
                father.put("path", nextNode.get("path"));
                next(father, nextNode);
            } else {
                father.put("children", nextLevelNodes);
            }

        }
    }

    @RequestMapping(value = {"/fileProp"}, method = RequestMethod.GET)
    public HttpResult fileProp(@RequestParam String appName) {
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("appPath", fileWalker.getAppPath());
        return new HttpResult(prop);
    }

}
