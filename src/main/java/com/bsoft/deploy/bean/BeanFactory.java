package com.bsoft.deploy.bean;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.file.FileSender;
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

    @Bean(initMethod = "start")
    SimpleFileServer fileServer() {
        return new SimpleFileServer();
    }

    @Bean
    FileSender fileSender() {
        return new FileSender();
    }

    @Bean
    FileWalkerFactory getFileWalkerFactory() {
        return new FileWalkerFactory();
    }

    @Bean
    Global initGlobal() {
        return new Global();
    }
}
