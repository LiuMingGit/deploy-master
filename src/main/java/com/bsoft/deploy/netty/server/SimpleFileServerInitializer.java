package com.bsoft.deploy.netty.server;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Slave;
import com.bsoft.deploy.netty.codec.MarshallingCodeCFactory;
import com.bsoft.deploy.service.SlaveService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * desc
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class SimpleFileServerInitializer extends
        ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //framer  DelimiterBasedFrameDecoder  表示结束帧的标记 /r/n
        pipeline.addLast("decoder", MarshallingCodeCFactory.buildMarshallingDecoder());
        pipeline.addLast("encoder", MarshallingCodeCFactory.buildMarshallingEncoder());
        pipeline.addLast("handler", new SimpleFileServerHandler());
        try {
            String ip = ch.remoteAddress().getHostName();
            Slave slave = Global.getSlaveStore().getSlave(ip);
            if (slave == null) {
                slave = new Slave();
                slave.setIp(ip);
                slave.setDescribes("请尽快完善节点描述信息");
                slave.setName("新节点");
                SlaveService slaveService = Global.getAppContext().getBean(SlaveService.class);
                slaveService.saveSlave(slave);
            }
        } catch (Exception e) {
           // silent
        }
        System.out.println("SimpleFileClient:"+ch.remoteAddress() +"连接上");

    }
}
