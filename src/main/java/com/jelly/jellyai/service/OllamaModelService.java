package com.jelly.jellyai.service;

import com.jelly.jellyai.entity.OllamaConfig;
import com.jelly.jellyai.repository.OllamaConfigRepository;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OllamaModelService {

    private OllamaApi ollamaApi;
    private String currentModel;
    private String baseUrl;
    
    @Autowired
    private OllamaConfigRepository ollamaConfigRepository;

    public OllamaModelService(@Value("${spring.ai.ollama.base-url}") String baseUrl,
                              @Value("${spring.ai.ollama.chat.options.model}") String defaultModel) {
        this.baseUrl = baseUrl;
        this.ollamaApi = new OllamaApi(baseUrl);
        this.currentModel = defaultModel;
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
    
    /**
     * 根据数据库配置更新Ollama服务地址
     */
    public void updateBaseUrlFromConfig() {
        OllamaConfig latestConfig = ollamaConfigRepository.findLatestConfig();
        if (latestConfig != null && latestConfig.getBaseUrl() != null && !latestConfig.getBaseUrl().isEmpty()) {
            this.baseUrl = latestConfig.getBaseUrl();
            // 重新创建OllamaApi实例
            this.ollamaApi = new OllamaApi(this.baseUrl);
            
            // 如果配置中包含默认模型，则更新当前模型
            if (latestConfig.getDefaultModel() != null && !latestConfig.getDefaultModel().isEmpty()) {
                this.currentModel = latestConfig.getDefaultModel();
            }
        }
    }
    
    /**
     * 使用指定的baseUrl更新OllamaApi实例
     * @param baseUrl 新的baseUrl
     */
    public void updateBaseUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            this.baseUrl = baseUrl;
            this.ollamaApi = new OllamaApi(this.baseUrl);
        }
    }
    
    /**
     * 获取当前baseUrl
     * @return 当前baseUrl
     */
    public String getBaseUrl() {
        return this.baseUrl;
    }
}