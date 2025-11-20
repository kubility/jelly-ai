package com.jelly.jellyai.message;

import com.jelly.jellyai.entity.JmsEntity;
import jakarta.jms.Destination;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PushService{
    @Autowired
    private DynamicMessageService dynamicMessageService;
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;
    @Autowired
    private Queue queue;
    @Autowired
    private Topic topic;


    public void sendTopic(String msg){
        System.out.println("发送主题消息: " + msg);
        sendMessage(topic,msg);
    }
    public void sendQueue(String msg){
        System.out.println("发送队列消息: " + msg);
        sendMessage(queue,msg);
    }

    public void sendQueue(JmsEntity entity){
        try {
            if (StringUtils.isBlank(entity.getDestination())) {
                entity.setDestination(queue.getQueueName());
            }
            // 为指定目的地创建动态监听器
            System.out.println("准备创建监听器，目的地：" + entity.getDestination());
            dynamicMessageService.createDynamicListener(entity.getDestination(), false);
            System.out.println("监听器创建完成，开始发送消息：" + entity.getMsg() + " 到目的地：" + entity.getDestination());
            sendMessage(entity.getDestination(), entity.getMsg());
            System.out.println("消息发送完成");
        } catch (Exception e) {
            System.err.println("发送消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendRetryableQueue(JmsEntity entity) {
        try {
            if (StringUtils.isBlank(entity.getDestination())) {
                entity.setDestination(queue.getQueueName() + ".retry");
            }
            // 为指定目的地创建可重试监听器
            System.out.println("准备创建可重试监听器，目的地：" + entity.getDestination());
            dynamicMessageService.createRetryableListener(entity.getDestination(), false);
            System.out.println("可重试监听器创建完成，开始发送消息：" + entity.getMsg() + " 到目的地：" + entity.getDestination());
            sendMessage(entity.getDestination(), entity.getMsg());
            System.out.println("可重试消息发送完成");
        } catch (Exception e) {
            System.err.println("发送可重试消息时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(Destination destination, String msg){
        jmsMessagingTemplate.convertAndSend(destination,msg);
    }
    public void sendMessage(String destination, String msg){
        jmsMessagingTemplate.convertAndSend(destination,msg);
    }

}
