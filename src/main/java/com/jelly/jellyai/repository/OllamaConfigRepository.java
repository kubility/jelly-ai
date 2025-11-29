package com.jelly.jellyai.repository;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.jelly.jellyai.entity.OllamaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@DS("slave")
public interface OllamaConfigRepository extends JpaRepository<OllamaConfig, Long> {

    // 获取最新的配置记录
    default OllamaConfig findLatestConfig() {
        return findAll().stream()
                .max((c1, c2) -> c1.getId().compareTo(c2.getId()))
                .orElse(null);
    }
}
