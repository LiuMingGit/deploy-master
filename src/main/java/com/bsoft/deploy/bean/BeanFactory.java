package com.bsoft.deploy.bean;

import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.context.store.AppStore;
import com.bsoft.deploy.context.store.SlaveStore;
import com.bsoft.deploy.context.store.TokenStore;
import com.bsoft.deploy.file.FileWalker;
import com.bsoft.deploy.netty.server.SimpleFileServer;
import com.bsoft.deploy.send.FileSender;
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
    SimpleFileServer createFileServer() {
        return new SimpleFileServer();
    }

    @Bean
    FileSender createFileSender() {
        return new FileSender();
    }


    @Bean
    Global initGlobal() {
        return new Global();
    }

    @Bean
    TokenStore createTokenStore() {
        return new TokenStore();
    }

    @Bean
    AppStore createAppStore() { return new AppStore(); }

    @Bean
    SlaveStore createSlaveStore() {
        return new SlaveStore();
    }

    @Bean
    FileWalker createFileWalker() {
        return new FileWalker();
    }
}
