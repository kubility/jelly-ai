package com.jelly.jellyai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.util.DisconnectedClientHelper;
import org.springframework.core.NestedExceptionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);
    
    /**
     * 自定义异常处理器，解决Spring Web 6.2.3中DisconnectedClientHelper的NPE问题
     */
    public static class CustomDisconnectedClientHelper {
        
        public static boolean isClientDisconnectedException(Throwable ex) {
            // 添加null检查避免NPE
            if (ex == null) {
                return false;
            }
            
            // 复现原始逻辑但增加保护
            try {
                Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
                if (cause == null) {
                    return false;
                }
                
                String message = cause.getMessage();
                if (message == null) {
                    return false;
                }
                
                // 检查常见的客户端断开连接情况
                return (cause instanceof java.io.EOFException ||
                       (cause instanceof java.io.IOException && (
                           message.contains("Broken pipe") || 
                           message.contains("Connection reset by peer") ||
                           message.contains("远程主机强迫关闭了一个现有的连接"))));
            } catch (Exception e) {
                // 如果检查过程中出现任何异常，记录日志并返回false
                logger.debug("Error checking for client disconnect: {}", e.getMessage());
                return false;
            }
        }
    }
}