package com.jelly.jellyai.message;

import jakarta.jms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicMessageService {
    private static final Map<String, DefaultMessageListenerContainer> listenersContainer=new ConcurrentHashMap<>();

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RetryableMessageListener retryableMessageListener;

    public DefaultMessageListenerContainer createDynamicListenerContainer(String destinationName,boolean isTopic) {
        DefaultMessageListenerContainer container =new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setPubSubDomain(isTopic);
        container.setDestinationName(destinationName);
        container.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE); // 添加确认模式
        container.setConcurrency("1-1"); // 设置并发
        container.setMessageListener((MessageListener) message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    System.out.println("收到动态消息: "+textMessage.getText() + " 来自队列: " + destinationName);
                }
                message.acknowledge();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });
        try {
            container.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        container.start();
        // 添加调试信息
        System.out.println("监听器已启动: " + container.isRunning());
        System.out.println("监听目的地: " + destinationName);

        return container;
    }

    public DefaultMessageListenerContainer createRetryableListenerContainer(String destinationName, boolean isTopic) {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setPubSubDomain(isTopic);
        container.setDestinationName(destinationName);
        container.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        container.setConcurrency("1-1");
        container.setMessageListener(retryableMessageListener);

        try {
            container.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        container.start();

        System.out.println("可重试监听器已启动: " + container.isRunning());
        System.out.println("监听目的地: " + destinationName);

        return container;
    }


    public void createDynamicListener(String destinationName,boolean isTopic) {
        synchronized (listenersContainer) {
            if(!listenersContainer.containsKey(destinationName)){
                DefaultMessageListenerContainer container = createDynamicListenerContainer(destinationName,isTopic);
                listenersContainer.put(destinationName,container);
                System.out.println("创建动态监听器成功:"+destinationName);
            } else {
                System.out.println("监听器已存在:"+destinationName);
            }
        }
    }

    public void createRetryableListener(String destinationName, boolean isTopic) {
        synchronized (listenersContainer) {
            if (!listenersContainer.containsKey(destinationName)) {
                DefaultMessageListenerContainer container = createRetryableListenerContainer(destinationName, isTopic);
                listenersContainer.put(destinationName, container);
                System.out.println("创建可重试监听器成功:" + destinationName);
            } else {
                System.out.println("监听器已存在:" + destinationName);
            }
        }
    }

    public void removeListener(String destinationName) {
        synchronized (listenersContainer) {
            DefaultMessageListenerContainer container = listenersContainer.get(destinationName);
            if(container!=null){
                container.stop();
                listenersContainer.remove(destinationName);
                System.out.println("移除动态监听器成功:"+destinationName);
            }
        }
    }
}
