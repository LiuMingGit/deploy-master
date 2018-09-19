package com.bsoft.deploy.file;

import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.*;
import com.bsoft.deploy.dao.mapper.AppFileMapper;
import com.bsoft.deploy.dao.mapper.AppMapper;
import com.bsoft.deploy.dao.mapper.SlaveAppFileMapper;
import com.bsoft.deploy.dao.mapper.SlaveMapper;
import com.bsoft.deploy.send.CmdSender;
import com.bsoft.deploy.send.FileSender;
import com.bsoft.deploy.utils.FileUtils;
import com.bsoft.deploy.utils.StringUtils;
import com.bsoft.deploy.utils.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 更新包同步
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
    @Autowired
    private SlaveAppFileMapper slaveAppFileMapper;

    /**
     * 记录正在同步的更新包信息
     */
    private ConcurrentHashMap<Integer, Guard> slaveApps = new ConcurrentHashMap<>();


    /**
     * 获取path路径下的文件列表
     *
     * @param path 文件路径
     * @return 文件列表
     */
    public List<Map<String, Object>> getFileTree(String path) {
        List<Map<String, Object>> files = new ArrayList<>();
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
    @Transactional
    public Map<String, Object> updateToSlave(int slaveAppId, int pkgId) {
        Map<String, Object> res = new HashMap<>();
        try {
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
                    res.put("message", MessageFormatter.arrayFormat("节点[{}]的应用[{}]回退成功!原始版本:{}->目标版本:{}", args).getMessage());
                }
            } else {
                // 设置更新进度跟踪
                Guard guard = new Guard();
                guard.setSlaveId(slave.getId());
                guard.setSlaveAppId(slaveApp.getId());
                guard.setPkgId(pkgId);
                slaveApps.put(slaveApp.getId(), guard);
                // 更新
                UpdateWorker worker = new UpdateWorker(slaveApp, guard, pkgId);
                worker.start();

                res.put("code", 9);
                Object[] args = new Object[]{(slaveAppPackage == null ? "空" : slaveAppPackage.getVersion()), appPackage.getVersion()};
                res.put("message", MessageFormatter.arrayFormat("原始版本:{}->目标版本:{},更新中,请稍后...", args).getMessage());
            }
        } catch (Exception e) {
            logger.error("应用更新失败!", e);
            res.put("code", 0);
            res.put("message", "应用更新失败!原因:" + e.getMessage());
        }

        return res;
    }

    private void updateVersion(int slaveAppId, int pkgId) {
        slaveMapper.updateSlaveAppVersion(slaveAppId, pkgId);
    }

    /**
     * 节点更新结果反馈
     *
     * @param result
     */
    public void reply(Map<String, Object> result) {
        int slaveAppId = (int) result.get("slaveAppId");
        boolean success = (boolean) result.get("success");
        if (slaveApps.containsKey(slaveAppId)) {
            Guard guard = slaveApps.get(slaveAppId);
            guard.setReceiveNum(1);
            if (!success) {
                guard.setFail(true);
            }
        }
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


    /**
     * 获取同步进度百分比
     *
     * @return 百分比
     */
    public Guard getGuard(int slaveAppId) {
        return slaveApps.get(slaveAppId);
    }

    public boolean isRunning(int slaveAppId) {
        return slaveApps.containsKey(slaveAppId);
    }

    public void finish(int slaveAppId) {
        Guard guard = slaveApps.get(slaveAppId);
        updateVersion(slaveAppId, guard.getPkgId());
        slaveApps.remove(slaveAppId);
    }

    public ConcurrentHashMap<Integer, Guard> getSlaveApps() {
        return slaveApps;
    }

}

class UpdateWorker extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(UpdateWorker.class);
    private SlaveApp slaveApp;
    private Guard guard;
    private int pkgId;

    private User user;
    @Autowired
    private AppMapper appMapper;
    @Autowired
    private SlaveAppFileMapper slaveAppFileMapper;

    public UpdateWorker(SlaveApp slaveApp, Guard guard, int pkgId) {
        this.slaveApp = slaveApp;
        this.guard = guard;
        this.pkgId = pkgId;
        user = UserUtils.getCurrentUser();
        appMapper = Global.getAppContext().getBean(AppMapper.class);
        slaveAppFileMapper = Global.getAppContext().getBean(SlaveAppFileMapper.class);
    }

    @Override
    public void run() {
        try {
            Slave slave = Global.getSlaveStore().getSlave(slaveApp.getSlaveId());
            AppPackage slaveAppPackage = appMapper.findSlaveAppPackage(slaveApp.getId());

            // 保存更新日志
            UpdateLog log = new UpdateLog();
            log.setSlaveAppId(slaveApp.getId());
            log.setOldPkgId(slaveAppPackage == null ? 0 : slaveAppPackage.getId());
            log.setNewPkgId(pkgId);
            log.setOptime(new Date());
            log.setOpuser(user.getId());
            slaveAppFileMapper.saveUpdateLog(log);
            guard.setUpdateId(log.getId());

            // 获取目标更新包之前最近的一个全量包id
            int lastFullPkgId = appMapper.findLastFullAppPackage(slaveApp.getAppId(), pkgId);
            // 未部署
            if (slaveAppPackage == null) {
                slaveAppPackage = new AppPackage();
                slaveAppPackage.setVersion("空");
                updates(slave, lastFullPkgId, pkgId);
            } else {
                int startPkgId = slaveAppPackage.getId();
                // 获取指定更新包与当前更新包之间的版本,若包含全量包,则从最近一个全量包开始
                if (lastFullPkgId > startPkgId) {
                    startPkgId = lastFullPkgId;
                    //跨全量包更新时,备份原有项目
                    backupSlaveApp(slaveApp);
                } else {
                    // 当前版本跳过
                    startPkgId = startPkgId + 1;
                }
                updates(slave, startPkgId, pkgId);
            }
        } catch (Exception e) {
            logger.error("更新包升级失败!", e);
            guard.setFail(true);
            guard.setMessage(e.getMessage());
        }
        // updateVersion(slaveApp.getId(), pkgId);
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

    private void updates(Slave slave, int startPkgId, int endPkgId) {
        List<AppPackage> packages = appMapper.findUpdates(slaveApp.getAppId(), startPkgId, endPkgId);
        if (packages.size() > 0) {
            for (AppPackage ap : packages) {
                List<FileDTO> files = appMapper.loadAppPackageFiles(ap.getId());
                guard.addTotalFiles(files.size());
                FileSender.handOut(files, slave, guard);
            }
        }
        if(guard.getTotalFiles() == 0) {
            guard.setFail(true);
            guard.setMessage("更新失败,更新包未包含任何有效文件!");
        }
    }


}
