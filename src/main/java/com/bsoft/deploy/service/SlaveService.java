package com.bsoft.deploy.service;

import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.dao.entity.Slave;
import com.bsoft.deploy.dao.entity.SlaveApp;
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
        return slaveMapper.findSlaveApps(appId);
    }

    public void saveSlave(Slave slave) {
        slaveMapper.save(slave);
    }

    public void updateSlave(Slave slave) {
        slaveMapper.update(slave);
        reload(slave.getId());
    }

    public void saveSlaveApp(SlaveApp slaveApp) {
        slaveMapper.saveSlaveApp(slaveApp);
    }

    public void updateSlaveApp(SlaveApp slaveApp) {
        slaveMapper.updateSlaveApp(slaveApp);
        reloadSlaveApp(slaveApp.getId());
    }

    public Slave findSlave(int id) {
        return slaveMapper.findSlave(id);
    }

    public Slave findSlave(String ip) {
        return slaveMapper.findSlaveByIp(ip);
    }

    public SlaveApp findSlaveApp(int id) {
        return slaveMapper.findSlaveAppById(id);
    }

    public void deleteSlave(int id) {
        slaveMapper.delete(id);
        reload(id);
    }

    public Map isTomcatRun(int slaveAppId) {
        try {
            return _handOut(Constant.CMD_IS_TOMCAT_RUN, slaveAppId);
        } catch (Exception e) {
            return null;
        }
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

    public void reload(int slaveId) {
        Global.getSlaveStore().reloadSlave(slaveId);

        Order order = new Order();
        order.setType(Constant.CMD_RELOAD_CACHE);
        Map<String, Object> req = new HashMap<>();
        req.put("target", "slave");
        // 0 表示全部
        req.put("id", slaveId);
        order.setReqData(req);
        CmdSender.handOut(order);
    }

    public void reloadSlaveApp(int slaveAppId) {
        Global.getSlaveStore().reloadSlaveApp(slaveAppId);
        Order order = new Order();
        order.setType(Constant.CMD_RELOAD_CACHE);
        Map<String, Object> req = new HashMap<>();
        req.put("target", "slaveApp");
        // 0 表示全部
        req.put("id", slaveAppId);
        order.setReqData(req);
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
