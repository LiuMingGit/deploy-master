package com.bsoft.deploy.bean;

import com.bsoft.deploy.file.FileWalker;
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
    @Bean(initMethod="start")
    FileWalker fileWalker() {
        return new FileWalker();
    }

}
