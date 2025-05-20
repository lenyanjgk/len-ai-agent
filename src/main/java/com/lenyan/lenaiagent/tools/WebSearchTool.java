package com.lenyan.lenaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具类
 */
public class WebSearchTool {

    // SearchAPI搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    // API密钥
    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 通过百度搜索查询信息
     */
    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 构建请求参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        
        try {
            // 发送搜索请求
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            
            // 解析返回结果
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");
            
            // 获取前5条结果并拼接
            return organicResults.stream()
                    .limit(5)
                    .map(obj -> ((JSONObject) obj).toString())
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}
