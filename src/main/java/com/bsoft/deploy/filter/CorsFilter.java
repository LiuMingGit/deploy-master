package com.bsoft.deploy.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跨域过滤器,允许接收跨域请求
 * 权限验证token
 * Created on 2018/8/8.
 *
 * @author yangl
 */
@Component
public class CorsFilter implements Filter {
    private final static Logger logger = LoggerFactory.getLogger(CorsFilter.class);
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type,X-Token");
        logger.debug("*********************************过滤器被使用**************************");

        System.out.println(request.getHeader("Origin"));
        String ticket = request.getHeader("X-Token");
        String path = request.getServletPath();
        System.out.println("path:" + path +",X-Token:" + ticket);
        if("OPTIONS".equals(request.getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if("/user/login".equals(path)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }else {
            // 验证票据合法性
            /*if (StringUtils.isEmpty(ticket) || Global.getTokenStore().get(ticket) == null) {
                HttpResult result = new HttpResult(50014,"权限验证失败!");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter out = response.getWriter();
                out.print(JSON.toJSONString(result));
                return;
            }*/
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
