package com.bsoft.deploy;

import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.netty.server.SimpleFileServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * desc
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class ServerSocketChannelDemo {


    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new SimpleFileServer(9999).start();
            }
        }).start();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            String input = in.readLine();
            if("send".equals(input)) {
                System.out.println(input);
                AppFile file = new AppFile("D:/workspace_ideal/deploy/master/src/main/java/com/bsoft/deploy/MasterApplication.java","D:/workspace_ideal/deploy/master/");
                // FileSender.handOut(file);
            }
        }
    }

}
