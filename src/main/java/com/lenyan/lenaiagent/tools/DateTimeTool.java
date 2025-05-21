package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期时间工具类
 */
@Component
public class DateTimeTool {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取当前日期时间
     */
    @Tool(description = "Get current date and time in specified format")
    public String getCurrentDateTime(
            @ToolParam(description = "Format pattern (e.g. yyyy-MM-dd HH:mm:ss)") String format
    ) {
        try {
            String formatPattern = (format == null || format.isEmpty()) ? DEFAULT_DATETIME_FORMAT : format;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            return LocalDateTime.now().format(formatter);
        } catch (Exception e) {
            return "Error getting current date time: " + e.getMessage();
        }
    }
} 