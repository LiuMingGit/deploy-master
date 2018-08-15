package com.bsoft.deploy.dao.entity;

/**
 * desc
 * Created on 2018/8/13.
 *
 * @author yangl
 */
public class App {
    private int appId;
    private String appName;
    private String path;

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
