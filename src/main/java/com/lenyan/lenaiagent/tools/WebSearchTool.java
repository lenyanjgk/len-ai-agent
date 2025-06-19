package com.lenyan.lenaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具类
 */
public class WebSearchTool {

    private static final Logger log = LoggerFactory.getLogger(WebSearchTool.class);

    // SearchAPI搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    // 搜索调用次数限制
    private static final int MAX_SEARCH_CALLS = 3;
    
    // 当前会话中的搜索调用次数
    private static int searchCallCount = 0;

    // API密钥
    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * 获取剩余的搜索调用次数
     */
    public static int getRemainingSearchCalls() {
        return Math.max(0, MAX_SEARCH_CALLS - searchCallCount);
    }
    
    /**
     * 重置搜索调用次数计数器
     */
    public static void resetSearchCallCount() {
        searchCallCount = 0;
        log.info("搜索调用次数计数器已重置");
    }

    /**
     * 通过百度搜索查询信息
     */
    @Tool(description = "Search for information from Baidu Search Engine (Limited to 3 calls per session)")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 检查调用次数限制
        if (searchCallCount >= MAX_SEARCH_CALLS) {
            return "搜索次数已达到限制（" + MAX_SEARCH_CALLS + "次），无法继续使用搜索工具";
        }
        
        // 增加调用计数
        searchCallCount++;
        log.info("执行搜索查询: '{}' (调用 {}/{})", query, searchCallCount, MAX_SEARCH_CALLS);
        
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        
        try {
            // 发送搜索请求
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            log.debug("搜索API响应: {}", response);
            
            // 解析返回结果
            JSONObject jsonObject = JSONUtil.parseObj(response);
            
            // 检查API是否返回错误
            if (jsonObject.containsKey("error")) {
                String errorMsg = jsonObject.getStr("error", "Unknown API error");
                log.warn("搜索API返回错误: {}", errorMsg);
                return "搜索API返回错误: " + errorMsg;
            }
            
            // 获取搜索结果，添加空检查
            if (!jsonObject.containsKey("organic_results") || jsonObject.isNull("organic_results")) {
                log.warn("搜索结果中没有 organic_results 字段或为空");
                return "未找到搜索结果";
            }
            
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                log.info("搜索结果为空数组");
                return "没有找到与 '" + query + "' 相关的搜索结果";
            }
            
            // 格式化搜索结果为更友好的格式
            return formatSearchResults(organicResults, query);
        } catch (Exception e) {
            log.error("搜索Baidu时发生错误", e);
            return "Error searching Baidu: " + e.getMessage();
        }
    }
    
    /**
     * 格式化搜索结果为更友好的输出格式
     */
    private String formatSearchResults(JSONArray results, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append("搜索 '").append(query).append("' 结果 (剩余调用次数: ").append(MAX_SEARCH_CALLS - searchCallCount).append("):\n\n");
        
        int count = Math.min(results.size(), 5);
        for (int i = 0; i < count; i++) {
            JSONObject result = results.getJSONObject(i);
            
            sb.append(i + 1).append(". ");
            
            // 标题
            if (result.containsKey("title")) {
                sb.append(result.getStr("title")).append("\n");
            }
            
            // 链接
            if (result.containsKey("link")) {
                sb.append("链接: ").append(result.getStr("link")).append("\n");
            }
            
            // 摘要
            if (result.containsKey("snippet")) {
                sb.append(result.getStr("snippet")).append("\n");
            }
            
            // 日期（如果有）
            if (result.containsKey("date")) {
                sb.append("日期: ").append(result.getStr("date")).append("\n");
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
