package com.bsoft.deploy.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.netty.server.SimpleFileServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * desc
 * Created on 2018/8/15.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/slave")
public class SlaveController {
    /**
     * 获取当前活跃的子节点信息
     * @return
     */
    @RequestMapping(value = {"/alive"}, method = RequestMethod.GET)
    public HttpResult aliveSlaveList() {
        ChannelGroup channels = SimpleFileServerHandler.channels;
        List<String> machines = new ArrayList<>();
        for(Channel ch : channels) {
            machines.add(ch.remoteAddress().toString());
        }
        return new HttpResult(machines);
    }

    /**
     * 获取所有的子节点信息
     * @return
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public HttpResult slaveList() {
        ChannelGroup channels = SimpleFileServerHandler.channels;
        List<String> machines = new ArrayList<>();
        for(Channel ch : channels) {
            machines.add(ch.remoteAddress().toString());
        }
        return new HttpResult(machines);
    }

    @RequestMapping(value = {"/sync"}, method = RequestMethod.POST)
    public HttpResult syncSlave(@RequestBody String jsonData) {
        Map<String, Object> body = (Map<String, Object>) JSONUtils.parse(jsonData);
        List<String> slaves = (List<String>) body.get("slaves");
        List<String> files = (List<String>) body.get("files");
        for(String slave : slaves) {

        }
        return new HttpResult();
    }
}
