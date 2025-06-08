package com.lenyan.lenaiagent.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图片搜索工具类
 */
@Component
public class ImageSearchTool {
    // 日志
    private static final Logger log = LoggerFactory.getLogger(ImageSearchTool.class);

    // 从配置文件注入Pexels API密钥
    @Value("${pexels.api-key}")
    private String apiKey;

    // Pexels 搜索接口
    private static final String API_URL = "https://api.pexels.com/v1/search";
    
    // HTTP请求超时时间（毫秒）
    private static final int TIMEOUT = 10000;

    /**
     * 搜索图片并返回markdown格式的图片链接
     */
    @Tool(description = "Search for images from the web using keywords")
    public String searchImage(@ToolParam(description = "Search query keyword") String query) {
        if (StrUtil.isBlank(query)) {
            return "搜索关键词不能为空";
        }
        
        try {
            log.info("正在搜索图片: {}", query);
            List<String> imageUrls = searchMediumImages(query);
            
            if (imageUrls.isEmpty()) {
                return "未找到与 '" + query + "' 相关的图片";
            }
            
            // 构建markdown格式的图片链接
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < imageUrls.size(); i++) {
                String markdownUrl = "![](" + imageUrls.get(i) + ")";
                sb.append(markdownUrl);
                
                // 为了更好的布局，每个图片后添加换行符
                if (i < imageUrls.size() - 1) {
                    sb.append("\n\n");
                }
            }
            
            log.info("搜索结果: {} 张图片", imageUrls.size());
            return sb.toString();
        } catch (Exception e) {
            log.error("图片搜索出错", e);
            return "图片搜索出错: " + e.getMessage();
        }
    }

    /**
     * 搜索中等尺寸的图片列表
     *
     * @param query 搜索关键词
     * @return 图片URL列表
     */
    private List<String> searchMediumImages(String query) {
        try {
            // 设置请求头（包含API密钥）
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", apiKey);

            // 设置请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("query", query);
            params.put("per_page", 5); // 限制返回结果数量
            params.put("locale", "zh-CN"); // 优先中文结果

            // 发送 GET 请求
            HttpResponse httpResponse = HttpUtil.createGet(API_URL)
                    .addHeaders(headers)
                    .form(params)
                    .timeout(TIMEOUT)
                    .execute();
            
            // 检查HTTP响应状态码
            int status = httpResponse.getStatus();
            if (status != 200) {
                log.error("API请求失败，状态码: {}, 响应内容: {}", status, httpResponse.body());
                return new ArrayList<>();
            }
            
            String response = httpResponse.body();
            log.debug("API响应: {}", response);

            // 检查响应是否为有效JSON
            if (!JSONUtil.isJson(response)) {
                log.error("API返回非JSON格式响应: {}", response);
                return new ArrayList<>();
            }

            // 解析响应JSON
            JSONObject jsonObject = JSONUtil.parseObj(response);
            
            // 检查是否存在错误信息
            if (jsonObject.containsKey("error")) {
                log.error("API返回错误: {}", jsonObject.getStr("error"));
                return new ArrayList<>();
            }
            
            // 检查photos数组是否存在
            if (!jsonObject.containsKey("photos") || jsonObject.getJSONArray("photos") == null) {
                log.error("API响应中未找到photos数组或为null");
                return new ArrayList<>();
            }
            
            JSONArray photosArray = jsonObject.getJSONArray("photos");
            if (photosArray.isEmpty()) {
                log.info("搜索结果为空，未找到匹配的图片");
                return new ArrayList<>();
            }

            return photosArray.stream()
                    .map(photoObj -> (JSONObject) photoObj)
                    .map(photoObj -> {
                        // 检查src对象是否存在
                        if (!photoObj.containsKey("src")) {
                            log.warn("图片数据缺少src属性");
                            return null;
                        }
                        return photoObj.getJSONObject("src");
                    })
                    .filter(srcObj -> srcObj != null)
                    .map(srcObj -> {
                        // 优先使用medium尺寸，如果不存在则尝试其他尺寸
                        if (srcObj.containsKey("medium")) {
                            return srcObj.getStr("medium");
                        } else if (srcObj.containsKey("small")) {
                            return srcObj.getStr("small");
                        } else if (srcObj.containsKey("original")) {
                            return srcObj.getStr("original");
                        } else {
                            log.warn("图片数据缺少可用的URL");
                            return null;
                        }
                    })
                    .filter(StrUtil::isNotBlank)
                    .collect(Collectors.toList());
        } catch (HttpException e) {
            log.error("HTTP请求出错: {}", e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("搜索图片过程中出错", e);
            return new ArrayList<>();
        }
    }
} 