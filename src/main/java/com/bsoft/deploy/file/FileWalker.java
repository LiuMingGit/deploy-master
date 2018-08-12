package com.bsoft.deploy.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 * 遍历指定目录中的所有文件
 * Created on 2018/8/6.
 *
 * @author yangl
 */

public class FileWalker {
    private final static Logger logger = LoggerFactory.getLogger(FileWalker.class);

    @Value("${appPath}")
    private String appPath;

    private List<String> javaFiles = new ArrayList<>();
    private List<String> jsFiles = new ArrayList<>();
    private List<String> otherFiles = new ArrayList<>();

    public List<String> find(String filename) {
        if (filename == null) {
            return null;
        }
        String type = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        switch (type) {
            case "java":
            case "class":
                return find(filename, javaFiles);
            case "js":
                return find(filename, jsFiles);
            default:
                return find(filename, otherFiles);
        }
    }

    /**
     * 获取path路径下的文件列表
     * @param path 文件路径
     * @return 文件列表
     */
    public List<Map<String,Object>> getFileTree(String path) {
        List<Map<String,Object>> files = new ArrayList<>();
        if(StringUtils.isEmpty(path)) {
            path = appPath;
        }
        File dir = new File(path);
        if(dir.isDirectory()) {
            for(File f : dir.listFiles()) {
                Map<String,Object> fileNode = new HashMap<>(4);
                fileNode.put("name",f.getName());
                fileNode.put("path",f.getAbsolutePath());
                fileNode.put("leaf",!f.isDirectory());
                files.add(fileNode);
            }
        }
        Collections.sort(files, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                boolean leaf1 = (boolean)o1.get("leaf");
                boolean leaf2 = (boolean)o2.get("leaf");
                if(leaf1 && !leaf2) {
                    return 1;
                } else if(!leaf1 && leaf2) {
                    return -1;
                } else {
                    return o1.get("name").toString().compareTo(o2.get("name").toString());
                }
            }
        });
        return files;
    }

    private List<String> find(String filename, List<String> files) {
        List<String> finds = new ArrayList<>();
        for (String file : files) {
            if (file.contains(filename)) {
                finds.add(file);
            }
        }
        return finds;
    }


    public void start() {
        logger.debug("**********************file walker start at {}*************************",appPath);
        //获取其file对象
        File dir = new File(appPath);
        getFiles(dir);

    }

    private void getFiles(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                // 递归遍历
                getFiles(f);
            } else {
                String file = f.getAbsolutePath();
                if (f.getName().endsWith("class") || f.getName().endsWith("java")) {
                    javaFiles.add(file);
                } else if (f.getName().endsWith("js")) {
                    jsFiles.add(file);
                } else {
                    otherFiles.add(file);
                }
            }
        }

    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }
}
