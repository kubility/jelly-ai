package com.jelly.jellyai.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ollama_config")
public class OllamaConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "base_url")
    private String baseUrl;
    
    @Column(name = "default_model")
    private String defaultModel;
    
    @Column(name = "temperature")
    private Double temperature;
    
    @Column(name = "top_p")
    private Double topP;
    
    @Column(name = "top_k")
    private Integer topK;
    
    // Constructors
    public OllamaConfig() {}
    
    public OllamaConfig(String baseUrl, String defaultModel) {
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getDefaultModel() {
        return defaultModel;
    }
    
    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Double getTopP() {
        return topP;
    }
    
    public void setTopP(Double topP) {
        this.topP = topP;
    }
    
    public Integer getTopK() {
        return topK;
    }
    
    public void setTopK(Integer topK) {
        this.topK = topK;
    }
    
    @Override
    public String toString() {
        return "OllamaConfig{" +
                "id=" + id +
                ", baseUrl='" + baseUrl + '\'' +
                ", defaultModel='" + defaultModel + '\'' +
                ", temperature=" + temperature +
                ", topP=" + topP +
                ", topK=" + topK +
                '}';
    }
}