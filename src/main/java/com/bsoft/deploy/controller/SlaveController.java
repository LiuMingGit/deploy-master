package com.bsoft.deploy.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Slave;
import com.bsoft.deploy.dao.entity.SlaveApp;
import com.bsoft.deploy.exception.SlaveOfflineException;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.netty.server.SimpleFileServerHandler;
import com.bsoft.deploy.service.AppService;
import com.bsoft.deploy.service.SlaveService;
import com.bsoft.deploy.utils.DateUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 子节点restful api
 * Created on 2018/8/15.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/slave")
public class SlaveController {
    private final static Logger logger = LoggerFactory.getLogger(SlaveController.class);

    @Autowired
    private SlaveService slaveService;
    @Autowired
    private AppService appService;

    /**
     * 统一异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public HttpResult exceptionHandler(Exception e) {

        String error = "系统发生了意外的状况,反馈给管理员,我们将及时处理!";
        if (e instanceof SlaveOfflineException) {
            error = e.getMessage();
        }
        logger.error("error", e);
        return new HttpResult(30000, error);
    }

    /**
     * 节点 新增 or 修改
     *
     * @return
     */
    @RequestMapping(value = {"/save"}, method = RequestMethod.POST)
    public HttpResult saveSlave(@RequestBody String jsonData) {
        Slave slave = JSON.parseObject(jsonData, Slave.class);
        if (com.bsoft.deploy.utils.StringUtils.isEmpty(slave.getId()) || slave.getId() == 0) {
            slaveService.saveSlave(slave);
        } else {
            slaveService.updateSlave(slave);
        }
        Global.getSlaveStore().reloadAll();
        return new HttpResult(slave);
    }

    /**
     * 节点 新增 or 修改
     *
     * @return
     */
    @RequestMapping(value = {"/app/save"}, method = RequestMethod.POST)
    public HttpResult saveSlaveApp(@RequestBody String jsonData) {
        SlaveApp slaveApp = JSON.parseObject(jsonData, SlaveApp.class);
        if (com.bsoft.deploy.utils.StringUtils.isEmpty(slaveApp.getId()) || slaveApp.getId() == 0) {
            slaveService.saveSlaveApp(slaveApp);
        } else {
            slaveService.updateSlaveApp(slaveApp);
        }
        Global.getSlaveStore().reloadAll();
        return new HttpResult(slaveApp);
    }

    /**
     * 获取当前活跃的子节点信息
     *
     * @return
     */
    @RequestMapping(value = {"/alive"}, method = RequestMethod.GET)
    public HttpResult aliveSlaveList() {
        ChannelGroup channels = SimpleFileServerHandler.channels;
        List<String> machines = new ArrayList<>();
        for (Channel ch : channels) {
            machines.add(ch.remoteAddress().toString());
        }
        return new HttpResult(machines);
    }

    /**
     * 获取所有的子节点信息
     *
     * @return
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public HttpResult slaveList() {
        List<Slave> slaves = slaveService.loadSlaves();
        return new HttpResult(slaves);
    }

    /**
     * 获取子节点
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/load"}, method = RequestMethod.GET)
    public HttpResult loadSlave(int id) {
        Slave slave = slaveService.findSlave(id);
        return new HttpResult(slave);
    }

    /**
     * 获取节点App信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/app"}, method = RequestMethod.GET)
    public HttpResult loadSlaveApp(int id) {
        SlaveApp slave = slaveService.findSlaveApp(id);
        return new HttpResult(slave);
    }

    /**
     * 删除子节点
     *
     * @param id
     * @return
     */
    @RequestMapping(value = {"/delete"}, method = RequestMethod.GET)
    public HttpResult deleteSlave(int id) {
        // 判断应用是否被使用
        int size = slaveService.loadSlaveApps(id).size();
        HttpResult result = new HttpResult();
        if (size > 0) {
            result.setCode(30001);
            result.setMessage("子节点存在有效的关联应用,请先取消绑定的应用!");
            return result;
        }
        slaveService.deleteSlave(id);
        return new HttpResult();
    }


    /**
     * 获取子节点的应用信息
     *
     * @return
     */
    @RequestMapping(value = {"/apps"}, method = RequestMethod.GET)
    public HttpResult slaveAppList(int slaveId) {
        List<SlaveApp> slaves = slaveService.loadSlaveApps(slaveId);
        for (SlaveApp slaveApp : slaves) {
            int pkgId = slaveApp.getPkgId();
            if (pkgId > 0) {
                slaveApp.setAppPackage(appService.findAppPackageById(slaveApp.getPkgId()));
                int appId = slaveApp.getAppId();
                slaveApp.setUpdate(appService.hasUpdateVersion(appId, pkgId));
            } else {
                slaveApp.setUpdate(true);
            }
        }
        return new HttpResult(slaves);
    }

    /**
     * 获取子节点应用的更新包列表
     *
     * @return
     */
    @RequestMapping(value = {"/updates"}, method = RequestMethod.GET)
    public HttpResult slaveAppUpdateList(int slaveAppId) {
        SlaveApp slaveApp = slaveService.findSlaveApp(slaveAppId);
        int pkgId = slaveApp.getPkgId();
        int appId = slaveApp.getAppId();
        return new HttpResult(appService.getUpdates(appId, pkgId));
    }


    /**
     * 节点更新指定的更新包
     *
     * @param slaveAppId
     * @param pkgId
     * @return
     */
    @RequestMapping(value = {"/update"}, method = RequestMethod.GET)
    public HttpResult slaveAppUpdate(int slaveAppId, int pkgId) {
        Map result = appService.slaveAppUpdate(slaveAppId, pkgId);
        return new HttpResult(result);
    }

    @RequestMapping(value = {"/sync"}, method = RequestMethod.POST)
    public HttpResult syncSlave(@RequestBody String jsonData) {
        Map<String, Object> body = (Map<String, Object>) JSONUtils.parse(jsonData);
        List<String> slaves = (List<String>) body.get("slaves");
        List<String> files = (List<String>) body.get("files");
        for (String slave : slaves) {
            // @TODO 待实现
        }
        return new HttpResult();
    }

    @RequestMapping(value = {"/tomcatAlive"}, method = RequestMethod.GET)
    public HttpResult tomcatAlive(int slaveAppId) {
        return new HttpResult(slaveService.isTomcatRun(slaveAppId));
    }


    @RequestMapping(value = {"/tomcatStart"}, method = RequestMethod.GET)
    public HttpResult tomcatStart(int slaveAppId) {
        return new HttpResult(slaveService.startTomcat(slaveAppId));
    }

    @RequestMapping(value = {"/tomcatStop"}, method = RequestMethod.GET)
    public HttpResult tomcatStop(int slaveAppId) {
        return new HttpResult(slaveService.stopTomcat(slaveAppId));
    }

    @RequestMapping(value = {"/reload"}, method = RequestMethod.GET)
    public HttpResult reload() {
        slaveService.reloadCache();
        return new HttpResult();
    }

    @RequestMapping(value = {"/dumpThread"}, method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> dumpThread(int slaveAppId) {
        byte[] threadBytes = (byte[]) slaveService.dumpThread(slaveAppId).get("threads");
        ByteArrayResource threadFile = new ByteArrayResource(threadBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=thread_" + DateUtils.getNow("yyyyMMddHHmmss") + ".txt");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(threadFile.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(threadFile);
    }


}
