package com.bsoft.deploy.bean;

import com.bsoft.deploy.file.FileWorker;
import com.bsoft.deploy.netty.server.SimpleFileSender;
import com.bsoft.deploy.netty.server.SimpleFileServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring bean
 * Created on 2018/8/9.
 *
 * @author yangl
 */
@Configuration
public class BeanFactory {

    @Bean
    FileWorker fileWorker() {
        return new FileWorker();
    }

    @Bean(initMethod = "run")
    SimpleFileServer fileServer() {
        return new SimpleFileServer();
    }

    @Bean
    SimpleFileSender fileSender() {
        return new SimpleFileSender();
    }

    @Bean
    FileWalkerFactory getFileWalkerFactory() {
        return new FileWalkerFactory();
    }
}
