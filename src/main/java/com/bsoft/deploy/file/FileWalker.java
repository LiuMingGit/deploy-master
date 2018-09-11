package com.bsoft.deploy.file;

import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.*;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.dao.mapper.AppMapper;
import com.bsoft.deploy.dao.mapper.SlaveMapper;
import com.bsoft.deploy.send.CmdSender;
import com.bsoft.deploy.send.FileSender;
import com.bsoft.deploy.utils.FileUtils;
import com.bsoft.deploy.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;

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
    @Autowired
    private AppFileMapper fileMapper;
    @Autowired
    private SlaveMapper slaveMapper;
    @Autowired
    private AppMapper appMapper;
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
    public void syncFile(int pkgId, String filename) {
        syncFile(pkgId, new File(filename));
    }

    /**
     * 单文件同步(文件系统和数据库状态)
     *
     * @param file 文件对象
     */
    public void syncFile(int pkgId, File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                syncFile(pkgId, child);
            }
        } else {
            saveFile(pkgId, file);
        }

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

    private void saveFile(int pkgId, File file) {
        FileDTO fileDTO = fileMapper.findPackageFile(pkgId, file.getAbsolutePath());
        if (fileDTO == null) {
            fileDTO = new FileDTO();
            fileDTO.setFilename(file.getName());
            fileDTO.setPkgId(pkgId);
            fileDTO.setPath(file.getAbsolutePath());
            fileDTO.setMark(FileUtils.getFileMd5(file));
            fileDTO.setOptime(new Date());
            fileMapper.savePackageFile(fileDTO);
        } else {
            String md5 = FileUtils.getFileMd5(file);
            if (!StringUtils.equals(fileDTO.getMark(), md5)) {
                // 更新文件
                fileDTO.setMark(md5);
                fileDTO.setOptime(new Date());
                fileMapper.updatePackageFile(fileDTO);
            }
        }
    }


    /**
     * 节点版本更新
     *
     * @param slaveAppId
     * @param pkgId
     */
    public Map<String, Object> updateToSlave(int slaveAppId, int pkgId) {
        Map<String, Object> res = new HashMap<>();
        SlaveApp slaveApp = slaveMapper.findSlaveAppById(slaveAppId);
        Slave slave = Global.getSlaveStore().getSlave(slaveApp.getSlaveId());
        // 判断目标节点是否适合此版本
        AppPackage appPackage = appMapper.findAppPackageById(pkgId);
        int appId = appPackage.getAppId();
        App app = Global.getAppStore().getApp(appId);
        AppPackage slaveAppPackage = appMapper.findSlaveAppPackage(slaveAppId);

        if (slaveAppPackage != null && slaveAppPackage.getId() >= appPackage.getId()) {
            if (slaveAppPackage.getId() == appPackage.getId()) {
                // 版本一致,忽略更新
                res.put("code", 1);
                res.put("message", "更新忽略(原版本一致)");
            } else {
                // 版本回退
                res.put("code", 1);
                Object[] args = new Object[]{slave.getIp(), app.getAppName(), slaveAppPackage.getVersion(), appPackage.getVersion()};
                res.put("message", MessageFormatter.arrayFormat("节点[{}]的应用[{}]回退成功!{}->{}", args));
            }
        } else {
            // 获取目标更新包之前最近的一个全量包id
            int lastFullPkgId = appMapper.findLastFullAppPackage(appId, pkgId);
            // 未部署
            if (slaveAppPackage == null) {
                updates(slave, appId, lastFullPkgId, pkgId);
            } else {
                int startPkgId = slaveAppPackage.getId();
                // 获取指定更新包与当前更新包之间的版本,若包含全量包,则从最近一个全量包开始
                if(lastFullPkgId > startPkgId) {
                    startPkgId = lastFullPkgId;
                    //跨全量包更新时,备份原有项目
                    backupSlaveApp(slaveApp);
                } else {
                    // 当前版本跳过
                    startPkgId = startPkgId + 1;
                }
                updates(slave, appId, startPkgId, pkgId);
            }
            updateVersion(slaveApp.getId(), pkgId);
            res.put("code", 1);
            Object[] args = new Object[]{slave.getIp(), app.getAppName(), slaveAppPackage.getVersion(), appPackage.getVersion()};
            res.put("message", MessageFormatter.arrayFormat("节点[{}]的应用[{}]更新成功!{}->{}", args));
        }

        return res;
    }

    /**
     * 全量更新前,备份原有应用
     *
     * @param slaveApp
     */
    private void backupSlaveApp(SlaveApp slaveApp) {
        Order order = new Order();
        order.setType(Constant.CMD_APP_BACKUP);
        Map<String, Object> req = new HashMap<>();
        req.put("slaveAppId", slaveApp.getId());
        order.setReqData(req);
        CmdSender.handOutSync(order, slaveApp.getSlaveId(), 30 * 1000);
    }

    private void updates(Slave slave, int appId, int startPkgId, int endPkgId) {
        List<AppPackage> packages = appMapper.findUpdates(appId, startPkgId, endPkgId);
        if (packages.size() > 0) {
            for (AppPackage ap : packages) {
                List<FileDTO> files = appMapper.loadAppPackageFiles(ap.getId());
                FileSender.handOut(files, slave);
            }
        }
    }

    private void updateVersion(int slaveAppId, int pkgId) {
        slaveMapper.updateSlaveAppVersion(slaveAppId, pkgId);
    }

    /**
     * 节点版本更新
     *
     * @param slaves
     * @param pkgId
     */
    public List<Map<String, Object>> updateToSlaves(int[] slaves, int pkgId) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (int slaveId : slaves) {
            response.add(updateToSlave(slaveId, pkgId));
        }
        return response;
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
