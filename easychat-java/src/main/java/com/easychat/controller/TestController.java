package com.easychat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @RequestMapping("/test")
    public String test(){
        return "哈哈我成功了！";
    }

    @RequestMapping("/testJrebel")
    public String testJrebel(){
        return "热部署成功";
    }
}
