package com.jelly.jellyai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChatEntity implements Serializable {
    @Schema(name = "msg",title = "msg", description = "消息",example = "你好")
    private String msg;

    @Schema(name = "webSearch", title = "webSearch", description = "是否启用网络搜索", example = "true")
    private Boolean webSearch = false;

    @Schema(name = "responseType", title = "responseType", description = "请求类型", example = "1")
    private String responseType;
    
    // 添加sessionId和messageId字段以匹配前端发送的数据
    @Schema(name = "sessionId", title = "sessionId", description = "会话ID", example = "session_123456")
    private String sessionId;
    
    @Schema(name = "messageId", title = "messageId", description = "消息ID", example = "msg_123456_user")
    private String messageId;
}