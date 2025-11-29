package com.jelly.jellyai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DateTimeTools {
    @Tool(description = "获取用户所在时区的当前日期和时间")
    public String getCurrentTime() {
        String time = LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        System.out.println("getCurrentTime:" + time);
        return time;
    }
}
