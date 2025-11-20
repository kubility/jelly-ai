package com.jelly.jellyai.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.jellyai.entity.ChatEntity;
import com.jelly.jellyai.service.OllamaModelService;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final OllamaModelService ollamaModelService;
    
    @Autowired
    public WebSocketHandler(OllamaModelService ollamaModelService) {
        this.ollamaModelService = ollamaModelService;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket连接已建立: " + session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            // 解析客户端发送的消息
            String payload = message.getPayload();
            ChatEntity chatEntity = objectMapper.readValue(payload, ChatEntity.class);
            
            // 使用当前模型的OllamaChatModel实例
            OllamaChatModel currentModel = ollamaModelService.getCurrentOllamaChatModel();
            
            // 使用Ollama模型生成回答
            Flux<String> response = currentModel.stream(chatEntity.getMsg());
            response.publishOn(Schedulers.boundedElastic()).subscribe(
                    data -> {
                        try {
                            // 使用Base64编码确保所有字符都能正确传输
                            String encodedData = Base64.getEncoder().encodeToString(data.getBytes("UTF-8"));
                            session.sendMessage(new TextMessage(encodedData));
                        } catch (IOException e) {
                            System.err.println("发送WebSocket消息失败: " + e.getMessage());
                        }
                    },
                    error -> {
                        System.err.println("处理Ollama响应时出错: " + error.getMessage());
                        try {
                            String errorMsg = "处理您的请求时出现错误: " + error.getMessage();
                            String encodedErrorMsg = Base64.getEncoder().encodeToString(errorMsg.getBytes("UTF-8"));
                            session.sendMessage(new TextMessage(encodedErrorMsg));
                        } catch (IOException e) {
                            System.err.println("发送错误消息失败: " + e.getMessage());
                        }
                    },
                    () -> {
                        try {
                            String doneMsg = "[DONE]";
                            String encodedDoneMsg = Base64.getEncoder().encodeToString(doneMsg.getBytes("UTF-8"));
                            session.sendMessage(new TextMessage(encodedDoneMsg));
                        } catch (IOException e) {
                            System.err.println("发送完成消息失败: " + e.getMessage());
                        }
                    }
            );
        } catch (Exception e) {
            System.err.println("处理WebSocket消息时出错: " + e.getMessage());
            try {
                String errorMsg = "处理您的请求时出现错误: " + e.getMessage();
                String encodedErrorMsg = Base64.getEncoder().encodeToString(errorMsg.getBytes("UTF-8"));
                session.sendMessage(new TextMessage(encodedErrorMsg));
            } catch (IOException ioException) {
                System.err.println("发送错误消息失败: " + ioException.getMessage());
            }
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("WebSocket连接已关闭: " + session.getId() + ", 状态: " + status);
    }
}