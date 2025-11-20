package com.jelly.jellyai.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticController {
    
    /**
     * 处理根路径请求，转发到聊天页面
     * @return 转发到聊天页面
     */
    @GetMapping("/")
    public String index() {
        return "forward:/chat/index.html";
    }
}