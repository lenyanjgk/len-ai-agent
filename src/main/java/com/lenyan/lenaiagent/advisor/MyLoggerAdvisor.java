package com.lenyan.lenaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor，打印用户输入和 AI 输出
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0; // 执行顺序
    }

    // 请求前打印用户输入
    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    // 响应后打印 AI 输出
    private void observeAfter(AdvisedResponse response) {
        //log.info("AI Response: {}", response.response().getResult().getOutput().getText());
    }

    // 同步调用处理
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest req, CallAroundAdvisorChain chain) {
        req = before(req);
        AdvisedResponse res = chain.nextAroundCall(req);
        observeAfter(res);
        return res;
    }

    // 流式调用处理
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest req, StreamAroundAdvisorChain chain) {
        req = before(req);
        Flux<AdvisedResponse> res = chain.nextAroundStream(req);
        return new MessageAggregator().aggregateAdvisedResponse(res, this::observeAfter);
    }
}