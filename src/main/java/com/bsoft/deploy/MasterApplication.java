package com.bsoft.deploy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * 遍历指定目录中的所有文件
 * Created on 2018/8/6.
 *
 * @author yangl
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.bsoft.deploy")
@MapperScan("com.bsoft.deploy.dao.mapper")
public class MasterApplication {
    private static ApplicationContext appContext;
    public static void main(String[] args) {

        appContext = SpringApplication.run(MasterApplication.class, args);
    }

    public static ApplicationContext get() {
        return appContext;
    }
}
