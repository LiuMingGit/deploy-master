package com.bsoft.deploy.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.bsoft.deploy.dao.entity.Slave;
import com.bsoft.deploy.dao.entity.SlaveApp;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.netty.server.SimpleFileServerHandler;
import com.bsoft.deploy.service.SlaveService;
import com.bsoft.deploy.utils.DateUtils;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
    @Autowired
    private SlaveService slaveService;

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
     * 获取子节点的应用信息
     *
     * @return
     */
    @RequestMapping(value = {"/apps"}, method = RequestMethod.GET)
    public HttpResult slaveAppList(int slaveId) {
        List<SlaveApp> slaves = slaveService.loadSlaveApps(slaveId);
        return new HttpResult(slaves);
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


}
