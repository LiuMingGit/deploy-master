package com.bsoft.deploy.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 遍历指定目录中的所有文件
 * Created on 2018/8/6.
 *
 * @author yangl
 */
public class FileWalker {
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

    private List<String> find(String filename, List<String> files) {
        List<String> finds = new ArrayList<>();
        for (String file : files) {
            if (file.indexOf(filename) >= 0) {
                finds.add(file);
            }
        }
        return finds;
    }


    public void start(String path) {
        //获取其file对象
        File dir = new File(path);
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
}
