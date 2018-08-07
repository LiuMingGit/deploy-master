package com.bsoft.deploy.controller;

import com.bsoft.deploy.dao.entity.User;
import com.bsoft.deploy.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * desc
 * Created on 2018/8/7.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/user")
public class LogonController {
    @Resource
    UserService userService;

    @RequestMapping(value={"/findUser"}, method= RequestMethod.GET)
    public User findUser(String logonName) {
        return userService.findUser(logonName);
    }
}
