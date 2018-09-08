package com.bsoft.deploy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 页面入口
 * Created on 2018/9/7.
 *
 * @author yangl
 */
@Controller
public class MainController {
    @RequestMapping("/")
    public String simple(ModelMap map){
        map.addAttribute("temp","new出蕾姆！");
        return "index.html";
    }
}
