package com.bsoft.deploy.service;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.App;
import com.bsoft.deploy.dao.entity.AppPackage;
import com.bsoft.deploy.dao.mapper.AppMapper;
import com.bsoft.deploy.file.FileWalker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * desc
 * Created on 2018/8/23.
 *
 * @author yangl
 */
@Service
public class AppService {
    @Autowired
    AppMapper appMapper;

    public List<App> loadApps() {
        return appMapper.loadApps();
    }


    public void saveApp(App app) {
        appMapper.save(app);
    }

    public void updateApp(App app) {
        appMapper.update(app);
    }

    public void deleteApp(int appId) {
        appMapper.delete(appId);
    }

    public List<AppPackage> loadAppPackages(int appId) {
        return appMapper.loadAppPackages(appId);
    }

    public AppPackage findAppPackageById(int pkgId) {
        return appMapper.findAppPackageById(pkgId);
    }

    public void saveAppPackage(AppPackage appPackage) {
        appMapper.saveAppPackage(appPackage);
    }

    public void updateAppPackage(AppPackage appPackage) {
        appMapper.updateAppPackage(appPackage);
    }

    /**
     * 查询是否有更新
     *
     * @param appId
     * @param pkgId
     */
    public boolean hasUpdateVersion(int appId, int pkgId) {
        return appMapper.hasUpdateVersion(appId, pkgId) > 0;
    }

    /**
     * 获取更新包,不包含传入的pkgId
     *
     * @param appId
     * @param pkgId
     * @return
     */
    public List<AppPackage> getUpdates(int appId, int pkgId) {
        return appMapper.getUpdates(appId, pkgId);
    }

    /**
     * 节点更新指点更新包
     *
     * @param slaveAppId
     * @param pkgId
     */
    @Transactional
    public Map<String,Object> slaveAppUpdate(int slaveAppId, int pkgId) {
        FileWalker fw = Global.getAppContext().getBean(FileWalker.class);
        return fw.updateToSlave(slaveAppId, pkgId);
    }
}
