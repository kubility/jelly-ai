package com.jelly.jellyai.controller;

import com.jelly.jellyai.entity.OllamaConfig;
import com.jelly.jellyai.service.OllamaConfigService;
import com.jelly.jellyai.service.OllamaModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ollama/config")
@Tag(name = "Ollama配置管理")
public class OllamaConfigController {
    
    @Autowired
    private OllamaConfigService ollamaConfigService;
    
    @Autowired
    private OllamaModelService ollamaModelService;
    
    /**
     * 获取所有配置
     */
    @GetMapping
    @Operation(summary = "获取所有Ollama配置")
    public ResponseEntity<List<OllamaConfig>> getAllConfigs() {
        return ResponseEntity.ok(ollamaConfigService.getAllConfigs());
    }
    
    /**
     * 根据ID获取配置
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取Ollama配置")
    public ResponseEntity<OllamaConfig> getConfigById(@PathVariable Long id) {
        OllamaConfig config = ollamaConfigService.getConfigById(id);
        if (config != null) {
            return ResponseEntity.ok(config);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取最新的配置
     */
    @GetMapping("/latest")
    @Operation(summary = "获取最新的Ollama配置")
    public ResponseEntity<OllamaConfig> getLatestConfig() {
        OllamaConfig config = ollamaConfigService.getLatestConfig();
        if (config != null) {
            return ResponseEntity.ok(config);
        } else {
            // 如果没有配置，返回默认配置
            OllamaConfig defaultConfig = new OllamaConfig();
            defaultConfig.setBaseUrl("http://192.168.83.128:30254");
            defaultConfig.setDefaultModel("deepseek-r1:1.5b");
            defaultConfig.setTemperature(0.7);
            defaultConfig.setTopP(0.9);
            defaultConfig.setTopK(40);
            return ResponseEntity.ok(defaultConfig);
        }
    }
    
    /**
     * 保存配置
     */
    @PostMapping
    @Operation(summary = "保存Ollama配置")
    public ResponseEntity<OllamaConfig> saveConfig(@RequestBody OllamaConfig config) {
        OllamaConfig savedConfig = ollamaConfigService.saveConfig(config);
        
        // 更新OllamaModelService中的baseUrl
        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
            ollamaModelService.updateBaseUrl(config.getBaseUrl());
        }
        
        return ResponseEntity.ok(savedConfig);
    }
    
    /**
     * 设置默认模型
     */
    @PostMapping("/set-default")
    @Operation(summary = "设置默认模型")
    public ResponseEntity<Map<String, String>> setDefaultModel(@RequestBody Map<String, String> payload) {
        String defaultModel = payload.get("defaultModel");
        if (defaultModel == null || defaultModel.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "默认模型不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 获取最新的配置并更新默认模型
        OllamaConfig latestConfig = ollamaConfigService.getLatestConfig();
        if (latestConfig == null) {
            latestConfig = new OllamaConfig();
            latestConfig.setBaseUrl("http://192.168.83.128:30254");
            latestConfig.setTemperature(0.7);
            latestConfig.setTopP(0.9);
            latestConfig.setTopK(40);
        }
        latestConfig.setDefaultModel(defaultModel);
        
        // 保存配置
        ollamaConfigService.saveConfig(latestConfig);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "默认模型设置成功: " + defaultModel);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除Ollama配置")
    public ResponseEntity<Map<String, String>> deleteConfig(@PathVariable Long id) {
        ollamaConfigService.deleteConfig(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "配置删除成功");
        return ResponseEntity.ok(response);
    }
}