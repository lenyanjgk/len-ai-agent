package com.lenyan.lenaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

@Component
public class HttpAiApiClient {
    public static String callQwenModel(String apiKey, String userMessage) {
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";
        JSONObject requestBody = JSONUtil.createObj()
                .set("model", "qwen-plus")
                .set("input", JSONUtil.createObj()
                        .set("messages", JSONUtil.createArray()
                                .put(JSONUtil.createObj().set("role", "system").set("content",
                                        "You are a helpful assistant."))
                                .put(JSONUtil.createObj().set("role", "user").set("content", userMessage))))
                .set("parameters", JSONUtil.createObj().set("result_format", "message"));

        return HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute()
                .body();
    }

    public static void main(String[] args) {
        System.out.println("Http调用：" + callQwenModel(TestApiKey.API_KEY, "你是谁？"));
    }
}