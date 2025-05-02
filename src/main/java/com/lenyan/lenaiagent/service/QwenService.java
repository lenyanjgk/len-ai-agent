package com.lenyan.lenaiagent.service;

import com.lenyan.lenaiagent.domain.request.QwenMultiModalRequest;
import com.lenyan.lenaiagent.domain.request.QwenTextRequest;
import com.lenyan.lenaiagent.domain.response.QwenResponse;
import io.reactivex.Flowable;

/**
 * 通义千问服务接口
 */
public interface QwenService {

    /**
     * 文本对话
     *
     * @param request 文本请求
     * @return 响应
     */
    QwenResponse chat(QwenTextRequest request);

    /**
     * 流式文本对话
     *
     * @param request 文本请求
     * @return 流式响应
     */
    Flowable<QwenResponse> streamChat(QwenTextRequest request);

    /**
     * 多模态对话（图像、视频、音频）
     *
     * @param request 多模态请求
     * @return 响应
     */
    QwenResponse multiModalChat(QwenMultiModalRequest request);

    /**
     * 流式多模态对话
     *
     * @param request 多模态请求
     * @return 流式响应
     */
    Flowable<QwenResponse> streamMultiModalChat(QwenMultiModalRequest request);
}