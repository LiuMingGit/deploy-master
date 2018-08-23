package com.bsoft.deploy.controller;

import com.alibaba.fastjson.JSON;
import com.bsoft.deploy.dao.entity.App;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.service.AppService;
import com.bsoft.deploy.service.SlaveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 主节点restful api
 * Created on 2018/8/20.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/app")
public class MasterController {
    private final static Logger logger = LoggerFactory.getLogger(MasterController.class);

    @Autowired
    AppService appService;

    @Autowired
    SlaveService slaveService;

    /**
     * 统一异常处理
     * @param e
     * @return
     */
    @ExceptionHandler
    public HttpResult exceptionHandler(Exception e) {
        String error = "系统发生了意外的状况,反馈给管理员,我们将及时处理!";
        logger.error("error", e);
        return new HttpResult(30000, error);
    }


    /**
     * 获取应用列表
     *
     * @return
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public HttpResult appList() {
        List<App> apps = appService.loadApps();
        return new HttpResult(apps);
    }

    @RequestMapping(value = {"/delete"}, method = RequestMethod.GET)
    public HttpResult deleteApp(int appId) {
        // 判断应用是否被使用
        int size = slaveService.loadSlaves(appId).size();
        HttpResult result = new HttpResult();
        if(size > 0) {
            result.setCode(30001);
            result.setMessage("应用已经被启用,请先删除节点引用信息!");
            return result;
        }
        appService.deleteApp(appId);
        return new HttpResult();
    }

    /**
     * 新增 or 修改
     *
     * @return
     */
    @RequestMapping(value = {"/create", "/update"}, method = RequestMethod.POST)
    public HttpResult saveApp(@RequestBody String jsonData) {
        App app = JSON.parseObject(jsonData, App.class);
        if (StringUtils.isEmpty(app.getAppId()) || app.getAppId() == 0) {
            appService.saveApp(app);
        } else {
            appService.updateApp(app);
        }
        return new HttpResult(app);
    }


}
