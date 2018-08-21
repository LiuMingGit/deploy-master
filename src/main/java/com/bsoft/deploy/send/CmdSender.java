package com.bsoft.deploy.send;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Order;
import com.bsoft.deploy.netty.server.SimpleFileServerHandler;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * desc
 * Created on 2018/8/19.
 *
 * @author yangl
 */
public class CmdSender {
    private final static Logger logger = LoggerFactory.getLogger(CmdSender.class);

    public static ConcurrentHashMap<String, Order> waiters = new ConcurrentHashMap<>();

    private static final long CMD_TIME_OUT = 20000;

    /**
     * 发送指令到目标节点(同步模式)
     *
     * @param order      指令内容
     * @param slaveAppId 目标节点应用id
     */
    public static void handOutSync(Order order, int slaveAppId) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            int slaveId = Global.getSlaveStore().getSlaveApp(slaveAppId).getSlaveId();
            String slaveIp = Global.getSlaveStore().getSlave(slaveId).getIp();
            if (ch.remoteAddress().toString().contains(slaveIp)) {
                CountDownLatch latch = new CountDownLatch(1);
                String uuid = UUID.randomUUID().toString();
                order.setUniqueId(uuid);
                order.setLatch(latch);
                waiters.put(uuid, order);
                ch.writeAndFlush(order);
                try {
                    latch.await(CMD_TIME_OUT, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    logger.error("命令[{}]调用失败!", order.getType(), e);
                }
                waiters.remove(uuid);
            }
        }
    }

    /**
     * 异步分发(异步模式)
     *
     * @param order 指令
     */
    public static void handOut(Order order) {
        ChannelGroup group = SimpleFileServerHandler.channels;
        for (Channel ch : group) {
            ch.writeAndFlush(order);
        }
    }
}


