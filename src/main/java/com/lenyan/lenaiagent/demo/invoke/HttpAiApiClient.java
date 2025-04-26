package com.lenyan.lenaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class HttpAiApiClient {
    
    public static String callQwenModel(String apiKey, String userMessage) {
        // 准备请求URL
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        
        // 构建请求体JSON
        JSONObject requestBody = JSONUtil.createObj()
            .set("model", "qwen-plus")
            .set("input", JSONUtil.createObj()
                .set("messages", JSONUtil.createArray()
                    .put(JSONUtil.createObj()
                        .set("role", "system")
                        .set("content", "You are a helpful assistant."))
                    .put(JSONUtil.createObj()
                        .set("role", "user")
                        .set("content", userMessage))
                ))
            .set("parameters", JSONUtil.createObj()
                .set("result_format", "message"));
        
        // 发送HTTP请求
        HttpResponse response = HttpRequest.post(url)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .body(requestBody.toString())
            .execute();
        
        // 返回响应结果
        return response.body();
    }
    
    public static void main(String[] args) {
        String apiKey = TestApiKey.API_KEY;
        String response = callQwenModel(apiKey, "你是谁？");
        System.out.println("Http调用："+ response);
    }
}