package com.bsoft.deploy.bean;

import com.bsoft.deploy.http.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * desc
 * Created on 2018/9/7.
 *
 * @author yangl
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/app/*","/slave/*","/file/*")
                .excludePathPatterns("/public/*");
    }
}
