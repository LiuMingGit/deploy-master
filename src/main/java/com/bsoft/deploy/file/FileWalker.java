package com.bsoft.deploy.file;

import com.bsoft.deploy.dao.entity.FileDTO;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.send.FileSender;
import com.bsoft.deploy.utils.FileUtils;
import com.bsoft.deploy.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private AppFileMapper fileMapper;
    /**
     * 同步百分比
     */
    private volatile int progressPercent = 0;
    private volatile boolean isSyncRunning = false;


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
     *
     * @param path 文件路径
     * @return 文件列表
     */
    public List<Map<String, Object>> getFileTree(String path) {
        List<Map<String, Object>> files = new ArrayList<>();
        if (StringUtils.isEmpty(path)) {
            path = appPath;
        }
        File dir = new File(path);
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                Map<String, Object> fileNode = new HashMap<>(4);
                fileNode.put("name", f.getName());
                fileNode.put("path", f.getAbsolutePath());
                fileNode.put("leaf", !f.isDirectory());
                files.add(fileNode);
            }
        }
        Collections.sort(files, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                boolean leaf1 = (boolean) o1.get("leaf");
                boolean leaf2 = (boolean) o2.get("leaf");
                if (leaf1 && !leaf2) {
                    return 1;
                } else if (!leaf1 && leaf2) {
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


    public void init() {
        logger.debug("**********************file walker start at {}*************************", appPath);
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

    /**
     * 单文件同步(文件系统和数据库状态)
     *
     * @param filename 文件名(全路径)
     */
    public void syncFile(String filename) {

    }

    /**
     * 同步文件与数据库的状态
     * 一般新建项目时调用
     *
     * @return 同步进度百分比
     */
    public int syncFiles(final int appId) {
        if (!isSyncRunning) {
            isSyncRunning = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doSync(appId);
                }
            }).start();
        }
        return progressPercent;

    }


    private void doSync(int appId) {
        int totalFilesCount = javaFiles.size() + jsFiles.size() + otherFiles.size();
        int onePointStep = totalFilesCount / 100;
        loop(appId, onePointStep, javaFiles);
        loop(appId, onePointStep, jsFiles);
        loop(appId, onePointStep, otherFiles);
        isSyncRunning = true;
    }

    private void loop(int appId, int onePointStep, List<String> files) {
        int loop = 0;
        for (String fileName : files) {
            File file = new File(fileName);
            FileDTO fileDTO = fileMapper.loadAppFile(appId, file.getAbsolutePath());
            if (fileDTO == null) {
                fileDTO = new FileDTO();
                fileDTO.setFilename(file.getName());
                fileDTO.setAppId(appId);
                fileDTO.setPath(file.getAbsolutePath());
                fileDTO.setMark(FileUtils.getFileMd5(file));
                fileDTO.setOptime(new Date());
                fileMapper.saveAppFile(fileDTO);
            } else {
                String md5 = FileUtils.getFileMd5(file);
                if (!StringUtils.equals(fileDTO.getMark(), md5)) {
                    // 更新文件
                    fileDTO.setMark(md5);
                    fileDTO.setOptime(new Date());
                    fileMapper.updateAppFile(fileDTO);
                }
            }
            loop++;
            if (onePointStep < 1) {
                progressPercent = 50;
            } else {
                if (loop == onePointStep) {
                    progressPercent++;
                    loop = 0;
                }
            }
        }
    }



    /**
     * 同步文件到目标节点
     *
     * @param appId
     */
    public void syncFilesToSlave(int appId) {
        List<FileDTO> files = fileMapper.loadAppFiles(appId);
        FileSender.handOut(files);
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public void setFileMapper(AppFileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * 获取同步进度百分比
     *
     * @return 百分比
     */
    public int getProgressPercent() {
        return progressPercent;
    }
}
