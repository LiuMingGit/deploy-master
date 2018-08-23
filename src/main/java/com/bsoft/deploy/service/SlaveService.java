package com.bsoft.deploy.service;

import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.dao.entity.Slave;
import com.bsoft.deploy.dao.entity.SlaveApp;
import com.bsoft.deploy.dao.mapper.SlaveAppFileMapper;
import com.bsoft.deploy.dao.mapper.SlaveMapper;
import com.bsoft.deploy.send.CmdSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * desc
 * Created on 2018/8/18.
 *
 * @author yangl
 */
@Service
public class SlaveService {
    @Autowired
    private SlaveMapper slaveMapper;

    @Autowired
    private SlaveAppFileMapper slaveAppFileMapper;

    public List<Slave> loadSlaves() {
        return slaveMapper.loadSlaves();
    }

    public List<SlaveApp> loadSlaveApps(int slaveId) {
        List<SlaveApp> slaves = slaveMapper.loadSlaveApps(slaveId);
        for (SlaveApp slaveApp : slaves) {
            String appName = Global.getAppStore().getApp(slaveApp.getAppId()).getAppName();
            slaveApp.setAppName(appName);
        }
        return slaves;
    }

    public List<SlaveApp> loadSlaves(int appId) {
        return slaveAppFileMapper.findSlaveApps(appId);
    }

    public Map isTomcatRun(int slaveAppId) {
        return _handOut(Constant.CMD_IS_TOMCAT_RUN, slaveAppId);
    }

    public Map startTomcat(int slaveAppId) {
        return _handOut(Constant.CMD_TOMCAT_START, slaveAppId);
    }

    public Map stopTomcat(int slaveAppId) {
        return _handOut(Constant.CMD_TOMCAT_STOP, slaveAppId);
    }

    public Map dumpThread(int slaveAppId) {
        return _handOut(Constant.CMD_THREAD_DUMP, slaveAppId);
    }

    public void reloadCache() {
        Order order = new Order();
        order.setType(Constant.CMD_RELOAD_CACHE);
        CmdSender.handOut(order);
    }

    private Map _handOut(String type, int target) {
        Order order = new Order();
        order.setType(type);
        Map<String, Object> reqData = new HashMap<>();
        reqData.put("slaveAppId", target);
        order.setReqData(reqData);
        CmdSender.handOutSync(order, target);
        return order.getRespData();
    }


}
