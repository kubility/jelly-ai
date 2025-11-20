package com.jelly.jellyai.service;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OllamaModelService {

    private final OllamaApi ollamaApi;
    private String currentModel;
    private final String baseUrl;

    public OllamaModelService(@Value("${spring.ai.ollama.base-url}") String baseUrl,
                              @Value("${spring.ai.ollama.chat.options.model}") String defaultModel) {
        this.ollamaApi = new OllamaApi(baseUrl);
        this.currentModel = defaultModel;
        this.baseUrl = baseUrl;
    }

    /**
     * 获取Ollama可用模型列表
     *
     * @return 模型名称列表
     */
    public List<String> listModels() {
        OllamaApi.ListModelResponse listModelResponse = ollamaApi.listModels();
        return listModelResponse.models().stream()
                .map(OllamaApi.Model::name)
                .toList();
    }

    /**
     * 切换当前使用的模型
     *
     * @param modelName 模型名称
     * @return 新创建的OllamaChatModel实例
     */
    public OllamaChatModel switchModel(String modelName) {
        this.currentModel = modelName;
        // 创建新的OllamaChatModel实例
        OllamaOptions options = OllamaOptions.builder()
                .model(modelName).build();
        return   OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(this.baseUrl))
                .defaultOptions(options)
                .build();
    }

    /**
     * 获取当前使用的模型
     *
     * @return 当前模型名称
     */
    public String getCurrentModel() {
        return this.currentModel;
    }

    /**
     * 获取当前模型的OllamaChatModel实例
     *
     * @return OllamaChatModel实例
     */
    public OllamaChatModel getCurrentOllamaChatModel() {
        OllamaOptions options = OllamaOptions.builder()
                .model(this.currentModel)
                .build();
        return   OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(this.baseUrl))
                .defaultOptions(options)
                .build();
    }
}
