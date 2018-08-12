package com.bsoft.deploy;

import com.bsoft.deploy.netty.client.SimpleFileClient;

import java.io.IOException;

/**
 * desc
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class ClientSocketChannelDemo {

    public static void main(String[] args) throws IOException {

        new SimpleFileClient("127.0.0.1",9999).run();
    }

}
