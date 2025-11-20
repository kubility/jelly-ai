package com.jelly.jellyai.message;

import jakarta.jms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RetryableMessageListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(RetryableMessageListener.class);

    // 最大重试次数
    @Value("${spring.activemq.retry.max-attempts:3}")
    private int maxAttempts;

    // 重试间隔基础时间（毫秒）
    @Value("${spring.activemq.retry.initial-delay:1000}")
    private long initialDelay;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void onMessage(Message message) {
        try {
            String messageText = getMessageText(message);
            //模拟业务处理失败
            if (messageText.contains("error")) {
                throw new RuntimeException("业务处理失败: " + messageText);
            }
            // 模拟处理耗时
            Thread.sleep(50);
            logger.debug("业务逻辑处理完成");
            // 处理成功
            logger.info("消息处理成功");
            message.acknowledge();

        } catch (Exception e) {
            handleException(message, e);
        }
    }

    /**
     * 处理异常：重试或发送到死信队列
     * @param message 消息
     * @param exception 异常
     */
    private void handleException(Message message, Exception exception) {
        try {
            int retryCount = getRetryCount(message);
            String messageText = getMessageText(message);

            // 检查是否超过最大重试次数
            if (retryCount >= maxAttempts) {
                // 发送到死信队列
                logger.error("消息处理失败超过最大重试次数({})，发送到死信队列, 最终错误原因: {}",
                    maxAttempts, exception.getMessage(), exception);

                // 确认原消息
                message.acknowledge();

                // 发送到死信队列
                String dlqDestination = "DLQ." + getMessageDestination(message);
                jmsTemplate.send(dlqDestination, session -> {
                    TextMessage dlqMessage = session.createTextMessage(messageText);
                    dlqMessage.setStringProperty("failureReason", exception.getMessage());
                    dlqMessage.setLongProperty("failureTimestamp", System.currentTimeMillis());
                    return dlqMessage;
                });

                logger.info("消息已发送到死信队列: {}", dlqDestination);
            } else {
                // 执行重试
                logger.warn("消息处理失败，准备第 {} 次重试, 错误信息: {}",
                    retryCount + 1, exception.getMessage());

                // 等待重试（指数退避）
                long delay = (long) (initialDelay * Math.pow(2, retryCount));
                Thread.sleep(delay);

                // 确认原消息
                message.acknowledge();

                // 获取目的地
                String destination = getMessageDestination(message);

                // 重新发送消息进行重试
                jmsTemplate.send(destination, session -> {
                    TextMessage retryMessage = session.createTextMessage(messageText);
                    retryMessage.setIntProperty("retryCount", retryCount + 1);
                    retryMessage.setStringProperty("originalMessageId", getOriginalMessageId(message));
                    retryMessage.setLongProperty("retryTimestamp", System.currentTimeMillis());
                    return retryMessage;
                });

                logger.info("消息已重新发送进行重试，延迟时间: {} ms", delay);
            }
        } catch (Exception e) {
            logger.error("处理异常时发生错误", e);
        }
    }

    // 辅助方法（这些不计入核心方法数量）

    private int getRetryCount(Message message) {
        try {
            if (message.propertyExists("retryCount")) {
                return message.getIntProperty("retryCount");
            }
        } catch (JMSException e) {
            logger.warn("获取消息重试次数属性失败", e);
        }
        return 0;
    }

    private String getOriginalMessageId(Message message) {
        try {
            if (message.propertyExists("originalMessageId")) {
                return message.getStringProperty("originalMessageId");
            }
            return message.getJMSMessageID();
        } catch (JMSException e) {
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private String getMessageDestination(Message message) {
        try {
            if (message.getJMSDestination() instanceof org.apache.activemq.command.ActiveMQDestination) {
                return ((org.apache.activemq.command.ActiveMQDestination) message.getJMSDestination()).getPhysicalName();
            }
        } catch (JMSException e) {
            logger.warn("获取消息目的地失败", e);
        }
        return "jelly.queue";
    }

    private String getMessageText(Message message) {
        try {
            if (message instanceof TextMessage) {
                return ((TextMessage) message).getText();
            } else {
                return message.toString();
            }
        } catch (JMSException e) {
            logger.error("无法解析消息内容", e);
            return "无法解析的消息";
        }
    }
}
