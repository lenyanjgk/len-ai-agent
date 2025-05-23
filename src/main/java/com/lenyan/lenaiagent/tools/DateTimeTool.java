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
    
    /**
     * 计算两个日期之间的差值
     */
    @Tool(description = "Calculate the difference between two dates")
    public String calculateDateDifference(
            @ToolParam(description = "Start date (yyyy-MM-dd)") String startDate,
            @ToolParam(description = "End date (yyyy-MM-dd), default to today if not provided") String endDate
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = (endDate == null || endDate.isEmpty()) 
                ? LocalDate.now() 
                : LocalDate.parse(endDate, formatter);
            
            Period period = Period.between(start, end);
            long days = Duration.between(start.atStartOfDay(), end.atStartOfDay()).toDays();
            
            return String.format("从 %s 到 %s：\n%d年%d月%d日（总共%d天）", 
                    startDate, end.format(formatter), 
                    period.getYears(), period.getMonths(), period.getDays(), days);
        } catch (DateTimeParseException e) {
            return "日期格式错误，请使用yyyy-MM-dd格式：" + e.getMessage();
        } catch (Exception e) {
            return "计算日期差值时出错：" + e.getMessage();
        }
    }
    
    /**
     * 计算从特定日期开始的未来日期
     */
    @Tool(description = "Calculate a future date based on days/months/years from start date")
    public String calculateFutureDate(
            @ToolParam(description = "Start date (yyyy-MM-dd), default to today if not provided") String startDate,
            @ToolParam(description = "Number of days to add") Integer days,
            @ToolParam(description = "Number of months to add") Integer months,
            @ToolParam(description = "Number of years to add") Integer years
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
            LocalDate start = (startDate == null || startDate.isEmpty()) 
                ? LocalDate.now() 
                : LocalDate.parse(startDate, formatter);
            
            LocalDate result = start;
            if (days != null && days != 0) {
                result = result.plusDays(days);
            }
            if (months != null && months != 0) {
                result = result.plusMonths(months);
            }
            if (years != null && years != 0) {
                result = result.plusYears(years);
            }
            
            return String.format("从 %s 开始，%s%s%s后的日期是: %s", 
                    start.format(formatter),
                    (days != null && days != 0) ? days + "天" : "",
                    (months != null && months != 0) ? months + "个月" : "",
                    (years != null && years != 0) ? years + "年" : "",
                    result.format(formatter));
        } catch (DateTimeParseException e) {
            return "日期格式错误，请使用yyyy-MM-dd格式：" + e.getMessage();
        } catch (Exception e) {
            return "计算未来日期时出错：" + e.getMessage();
        }
    }
} 