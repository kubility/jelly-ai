package com.jelly.jellyai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatBase {
    @Schema(name = "prompt", title = "prompt", description = "提示词", example = "你好")
    private String prompt;
    @Schema(name = "model", title = "model", description = "模型", example = "qwen3-vl:2b")
    private String model;
    @Schema(name = "temperature", title = "temperature", description = "温度", example = "0.7")
    private Double temperature=0.2;
    @Schema(name = "max_tokens", title = "max_tokens", description = "最大token数", example = "1024")
    private Integer max_tokens=1024;
    @Schema(name = "stream", title = "stream", description = "是否流式返回", example = "true")
    private Boolean stream=true;
}
