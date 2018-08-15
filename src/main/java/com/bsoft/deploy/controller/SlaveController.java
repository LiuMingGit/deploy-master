package com.bsoft.deploy.controller;

import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.netty.server.SimpleFileServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
     * 获取当前已连接的节点信息
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
}
