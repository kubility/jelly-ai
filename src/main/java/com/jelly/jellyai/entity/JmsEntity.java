package com.jelly.jellyai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class JmsEntity {
    @Schema(name = "destination",title = "destination", description = "消息队列名",example = "queue")
    private String destination;
    @Schema(name = "msg",title = "msg", description = "消息",example = "你好")
    private String msg;
}
