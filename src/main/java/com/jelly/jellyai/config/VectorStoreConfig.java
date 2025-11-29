package com.jelly.jellyai.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class VectorStoreConfig {
    @Bean
    public VectorStore vectorStore(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 向向量存储中添加文档
        simpleVectorStore.add(new ArrayList<>(){{
            // 张三介绍
            add(new Document("张三，男，汉族，1996 年 7 月 26 日出生，现年 30 岁，毕业于四川大学计算机工程系。" +
                    "凭借扎实的专业教育背景，他在技术领域展现出强劲实力。\n" +
                    "张三尤其擅长算法设计与优化，能以高效逻辑解决复杂问题；在编程语言方面，对 C/C++ 运用娴熟，凭借该语言的高性能优势，" +
                    "完成众多高质量项目。同时，他在游戏开发领域造诣颇深，从游戏逻辑搭建到画面渲染优化，都能出色完成。" +
                    "此外，他对 AI 有着深入研究，善于将 AI 技术融入游戏开发，为游戏增添智能化体验，是兼具多元技能与创新思维的复合型技术人才。"));
        }});

        return simpleVectorStore;
    }
}
