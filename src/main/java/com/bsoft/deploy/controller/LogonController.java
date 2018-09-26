package com.bsoft.deploy.controller;

import com.alibaba.druid.support.json.JSONUtils;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.Token;
import com.bsoft.deploy.dao.entity.User;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * 登录管理 restful api
 * Created on 2018/8/7.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/user")
public class LogonController {
    private final static Logger logger = LoggerFactory.getLogger(LogonController.class);
    private final static String X_TOKEN_KEY = "token";
    @Resource
    UserService userService;


    @RequestMapping(value = {"/info"}, method = RequestMethod.GET)
    public HttpResult info(@RequestParam String ticket) {
        Token token =Global.getTokenStore().get(ticket);
        HttpResult result = new HttpResult();
        if(token != null) {
            User user = userService.findUserById(token.getUid());
            user.setRoles(Arrays.asList("admin"));
            result.setData(user);
        }
        return result;
    }


    @RequestMapping(value = {"/login"}, method = RequestMethod.POST)
    public HttpResult login(HttpServletRequest request,@RequestBody String jsonData) {
        Map<String, Object> body = (Map<String, Object>) JSONUtils.parse(jsonData);
        String logonName = (String) body.get("username");
        String password = (String) body.get("password");
        User user = userService.findUserByName(logonName);
        if (user == null || password == null) {
            return new HttpResult(50001, "用户不存在");
        }
        if (!password.equals(user.getPassword())) {
            return new HttpResult(50002, "用户密码不正确");
        }
        // @TODO jwt token
        String ticket = UUID.randomUUID().toString();
        user.setToken(ticket);
        Token token = new Token();
        token.setUid(user.getId());
        token.setTicket(ticket);
        Global.getTokenStore().set(token);
        return new HttpResult(20000, "success", user);
    }
}
