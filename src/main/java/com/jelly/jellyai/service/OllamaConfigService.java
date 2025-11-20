package com.jelly.jellyai.service;

import com.jelly.jellyai.entity.OllamaConfig;
import com.jelly.jellyai.repository.OllamaConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OllamaConfigService {
    
    @Autowired
    private OllamaConfigRepository ollamaConfigRepository;
    
    /**
     * 获取所有配置
     */
    public List<OllamaConfig> getAllConfigs() {
        return ollamaConfigRepository.findAll();
    }
    
    /**
     * 根据ID获取配置
     */
    public OllamaConfig getConfigById(Long id) {
        return ollamaConfigRepository.findById(id).orElse(null);
    }
    
    /**
     * 保存配置
     */
    public OllamaConfig saveConfig(OllamaConfig config) {
        return ollamaConfigRepository.save(config);
    }
    
    /**
     * 删除配置
     */
    public void deleteConfig(Long id) {
        ollamaConfigRepository.deleteById(id);
    }
    
    /**
     * 获取最新的配置
     */
    public OllamaConfig getLatestConfig() {
        return ollamaConfigRepository.findLatestConfig();
    }
}