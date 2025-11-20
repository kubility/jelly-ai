package com.jelly.jellyai.controller;

import com.jelly.jellyai.entity.ChatEntity;
import com.jelly.jellyai.service.OllamaModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.Base64;
import java.util.concurrent.Executors;

@RestController
@Tag(name = "Chat")
public class ChatController {

    // 存储会话ID和对应的聊天实体
    private final Map<String, ChatEntity> chatSessionMap = new ConcurrentHashMap<>();
    // 创建一个固定大小的线程池用于处理 SSE
    private final ExecutorService sseExecutor = Executors.newFixedThreadPool(10);

    @Resource
    @Qualifier("ollamaChatModel")
    private OllamaChatModel ollamaChatModel;

    // 注入Ollama模型服务
    private final OllamaModelService ollamaModelService;

    public ChatController(OllamaModelService ollamaModelService) {
        this.ollamaModelService = ollamaModelService;
    }

    @PostMapping("/api/ai/chat")
    @ResponseBody
    @Operation(summary = "ollama请求")
    public Object apiCommon(@RequestBody ChatEntity chatEntity) {
        String responseType = StringUtils.isBlank(chatEntity.getResponseType()) ? "4" : chatEntity.getResponseType();
        switch (responseType) {
            case "1":
                return ollamaAnswer(chatEntity);
            case "2":
                return ollamaAnswer2(chatEntity);
            case "4":
                return initSSESession(chatEntity);
            case "5":
                // WebSocket模式，返回成功标识，由前端通过WebSocket连接处理
                Map<String, Object> result = new HashMap<>();
                result.put("status", "success");
                result.put("message", "WebSocket连接已准备就绪");
                return result;
            default:
                return "请选择正确的请求类型";
        }
    }


    @PostMapping("/chat")
    @ResponseBody
    @Operation(summary = "ollama请求")
    public Object ollamaAnswer(@RequestBody ChatEntity chatEntity) {
        String response;
        try {
            // 使用当前模型的OllamaChatModel实例
            OllamaChatModel currentModel = ollamaModelService.getCurrentOllamaChatModel();
            response = currentModel.call(chatEntity.getMsg());
        } catch (Exception e) {
            response = "抱歉，处理您的请求时出现错误: " + e.getMessage();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("html", response);
        return map;
    }

    @PostMapping("/chat2")
    @ResponseBody
    @Operation(summary = "ollama请求流")
    public Flux<String> ollamaAnswer2(@RequestBody ChatEntity chatEntity) {
        // 使用当前模型的OllamaChatModel实例
        OllamaChatModel currentModel = ollamaModelService.getCurrentOllamaChatModel();
        Flux<String> call = currentModel.stream(chatEntity.getMsg());

        return call;
    }

    /* @PostMapping("/chat3")
    @ResponseBody
    public Flux<String> ollamaAnswer3(@RequestBody ChatEntity chatEntity) {
        return dashScopeChatModel.stream(chatEntity.getMsg());

    }*/

    /**
     * 初始化SSE会话
     *
     * @param chatEntity 聊天实体
     * @return 会话ID
     */
    @PostMapping("/chat4")
    @ResponseBody
    public String initSSESession(@RequestBody ChatEntity chatEntity) {
        // 生成唯一的会话ID
        String sessionId = UUID.randomUUID().toString();
        // 存储会话信息
        chatSessionMap.put(sessionId, chatEntity);
        return sessionId;
    }

    /**
     * SSE流式传输
     *
     * @param sessionId 会话ID
     * @return ResponseBodyEmitter
     */
    @GetMapping(value = "/chat4", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter streamSSE(@RequestParam String sessionId) {
        SseEmitter emitter = new SseEmitter(0L); // 设置10分钟超时时间
        // 检查会话是否存在
        if (sessionId == null || sessionId.isEmpty() || !chatSessionMap.containsKey(sessionId)) {
            sendSafe(emitter, "错误：会话不存在或已过期");
            completeSafe(emitter);
            return emitter;
        }
        // 获取聊天实体
        ChatEntity chatEntity = chatSessionMap.remove(sessionId);
        // 使用当前模型的OllamaChatModel实例
        OllamaChatModel currentModel = ollamaModelService.getCurrentOllamaChatModel();
        // 使用Ollama模型生成回答
        sseExecutor.submit(() -> {
            try {
                // 调用Ollama模型生成回答
                Flux<String> response = currentModel.stream(chatEntity.getMsg());
                response.publishOn(Schedulers.boundedElastic()).subscribe(
                        data -> sendSafe(emitter, data),
                        error -> {
                            System.out.println("报错了吗........");
                            String errorMsg = "流式传输过程中出错: " + error.getMessage();
                            sendSafe(emitter, errorMsg);
                        },
                        () -> {
                            sendSafe(emitter, "[DONE]");
                            completeSafe(emitter);
                        }
                );
            } catch (Exception e) {
                String errorMsg = "处理请求时出错: " + e.getMessage();
                sendSafe(emitter, errorMsg);
                completeSafe(emitter);
            }
        });

        return emitter;
    }

    /**
     * 安全发送数据
     *
     * @param emitter SseEmitter
     * @param data    数据
     */
    private void sendSafe(SseEmitter emitter, String data) {
        try {
            // 使用Base64编码确保所有字符都能正确传输
            String encodedData = Base64.getEncoder().encodeToString(data.getBytes("UTF-8"));
            emitter.send(SseEmitter.event().data(encodedData));
        } catch (IllegalStateException | IOException ignored) {
            // 客户端断开连接时可能会出现这些异常，属于正常情况
        }
    }

    /**
     * 安全完成emitter
     */
    private void completeSafe(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (IllegalStateException ignored) {
            // 客户端断开连接时可能会出现此异常，属于正常情况
        }
    }

    /**
     * 获取Ollama可用模型列表
     *
     * @return 模型列表
     */
    @GetMapping("/api/ai/models")
    @ResponseBody
    @Operation(summary = "获取Ollama模型列表")
    public List<String> listOllamaModels() {
        return ollamaModelService.listModels();
    }

    /**
     * 切换Ollama模型
     *
     * @param modelName 模型名称
     * @return 切换结果
     */
    @PostMapping("/api/ai/model/switch")
    @ResponseBody
    @Operation(summary = "切换Ollama模型")
    public Map<String, Object> switchOllamaModel(@RequestParam String modelName) {
        ollamaModelService.switchModel(modelName);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "模型已切换到: " + modelName);
        result.put("currentModel", ollamaModelService.getCurrentModel());
        return result;
    }

}