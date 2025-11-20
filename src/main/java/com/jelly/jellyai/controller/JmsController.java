package com.jelly.jellyai.controller;

import com.jelly.jellyai.entity.JmsEntity;
import com.jelly.jellyai.message.PushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Jms")
public class JmsController {

    @Value("${spring.activemq.queue_ack}")
    private String queue_ack;
    @Autowired
    private PushService pushService;
    @PostMapping("/queue")
    @Operation(summary = "queue")
    public String queue(@RequestBody JmsEntity jmsEntity) {
        System.out.println("发送消息到默认队列: " + jmsEntity.getMsg());
        pushService.sendQueue(jmsEntity.getMsg());
        return "success";
    }
    @PostMapping("/topic")
    @Operation(summary = "topic")
    public String topic(@RequestBody JmsEntity jmsEntity) {
        System.out.println("发布消息到默认主题: " + jmsEntity.getMsg());
        pushService.sendTopic(jmsEntity.getMsg());
        return "success";
    }
    @PostMapping("/queue_ack")
    @Operation(summary = "queue_ack")
    public String queue_ack(@RequestBody JmsEntity jmsEntity) {
        jmsEntity.setDestination(queue_ack);
        System.out.println("发送消息到确认队列: " + jmsEntity.getMsg() + " 目的地: " + jmsEntity.getDestination());
        pushService.sendQueue(jmsEntity);
        return "success";
    }

    @PostMapping("/queue_any")
    @Operation(summary = "queue_any")
    public String queue_ack_test(@RequestBody JmsEntity jmsEntity) {
        if(StringUtils.isBlank(jmsEntity.getDestination())){
            jmsEntity.setDestination(queue_ack);
        }
        System.out.println("发送消息到任意队列: " + jmsEntity.getMsg() + " 目的地: " + jmsEntity.getDestination());
        pushService.sendQueue(jmsEntity);
        return "success";
    }
    
    @PostMapping("/queue_retry")
    @Operation(summary = "queue_retry")
    public String queueRetry(@RequestBody JmsEntity jmsEntity) {
        System.out.println("发送可重试消息: " + jmsEntity.getMsg());
        pushService.sendRetryableQueue(jmsEntity);
        return "success";
    }
}