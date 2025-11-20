package com.jelly.jellyai.message;

import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PullService {

    @JmsListener(destination = "${spring.activemq.topic}",containerFactory = "topicJmsListenerContainerFactory")
    public void pullTopic(String msg) {
        log.info("接收到Topic:{}",msg);
    }


    @JmsListener(destination = "${spring.activemq.queue}",containerFactory = "jmsListenerContainerFactory")
    public void pullQueue(String msg) {
        log.info("接收到Queue-jelly-queue:{}",msg);
    }
    int num=0;
    @JmsListener(destination = "${spring.activemq.queue_ack}",containerFactory = "jmsListenerContainerFactory")
    public void pullQueue_Ack(ActiveMQMessage message, Session session) throws Exception {
         if(message instanceof TextMessage){
             TextMessage textMessage = (TextMessage) message;
             String text=textMessage.getText();
             Thread.sleep(10000);
             try {
                 if (text.contains("您好")) {
                     throw new Exception("手动抛出异常");
                 }
                 message.acknowledge();//手动确认
                 System.out.println("发送成功,未抛出异常"+text);
             }catch (Exception e) {
                 ++num;
                 log.info(String.format("触发重发机制，num = %s, msg = %s", num, text));
                 session.recover();
             }
         }
    }



}
