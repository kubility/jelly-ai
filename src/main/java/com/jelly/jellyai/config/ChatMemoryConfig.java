package com.jelly.jellyai.config;

import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import lombok.Data;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Data
@Configuration
@ConfigurationProperties(prefix="spring.data.redis")
public class ChatMemoryConfig {

    private String host;
    private int port;
    private String password;
    private String database;

    @Bean
    @Primary
    public ChatMemory chatMemory(JdbcChatMemoryRepository mysqlJdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(mysqlJdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
    @Bean("redisChatMemory")
    public ChatMemory redisChatMemory(JedisRedisChatMemoryRepository jedisRedisChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jedisRedisChatMemoryRepository)
                .maxMessages(10)
                .build();
    }
    @Bean
    public JedisRedisChatMemoryRepository jedisRedisChatMemoryRepository() {
        return JedisRedisChatMemoryRepository.builder()
                .host(host)
                .port(port)
                .password(password).build();
    }

    @Bean("mysqlJdbcChatMemoryRepository")
    public JdbcChatMemoryRepository mysqlJdbcChatMemoryRepository(DataSource mysqlDataSource) {
        return JdbcChatMemoryRepository.builder().dataSource(mysqlDataSource).build();
    }
    @Bean("h2JdbcChatMemoryRepository")
    public JdbcChatMemoryRepository h2JdbcChatMemoryRepository(DataSource h2DataSource) {
        return JdbcChatMemoryRepository.builder().dataSource(h2DataSource).build();
    }


}
