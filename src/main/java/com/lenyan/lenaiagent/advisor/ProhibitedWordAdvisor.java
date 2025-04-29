package com.lenyan.lenaiagent.advisor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 违禁词校验 Advisor
 * 检查用户输入和AI响应中是否包含违禁词
 */
@Slf4j
public class ProhibitedWordAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 违禁词列表
     */
    private final List<String> prohibitedWords;

    /**
     * 是否检查用户输入
     */
    private final boolean checkUserInput;

    /**
     * 是否检查AI响应
     */
    private final boolean checkAiResponse;

    /**
     * 是否使用正则表达式匹配
     */
    private final boolean useRegex;

    /**
     * 正则表达式缓存
     */
    private List<Pattern> compiledPatterns;

    /**
     * 默认违禁词文件路径
     */
    private static final String DEFAULT_PROHIBITED_WORDS_FILE = "prohibited-words.txt";

    /**
     * 创建一个默认的违禁词Advisor，从默认文件读取违禁词列表
     */
    public ProhibitedWordAdvisor() {
        this(loadProhibitedWordsFromFile(DEFAULT_PROHIBITED_WORDS_FILE), true, false, false);
    }

    /**
     * 创建一个默认的违禁词Advisor，从指定文件读取违禁词列表
     * 
     * @param prohibitedWordsFile 违禁词文件路径（相对于classpath）
     */
    public ProhibitedWordAdvisor(String prohibitedWordsFile) {
        this(loadProhibitedWordsFromFile(prohibitedWordsFile), true, false, false);
    }

    /**
     * 创建一个自定义违禁词Advisor
     * 
     * @param prohibitedWords 违禁词列表
     * @param checkUserInput  是否检查用户输入
     * @param checkAiResponse 是否检查AI响应
     * @param useRegex        是否使用正则表达式匹配
     */
    public ProhibitedWordAdvisor(List<String> prohibitedWords, boolean checkUserInput, boolean checkAiResponse,
            boolean useRegex) {
        this.prohibitedWords = prohibitedWords;
        this.checkUserInput = checkUserInput;
        this.checkAiResponse = checkAiResponse;
        this.useRegex = useRegex;

        if (useRegex) {
            compilePatterns();
        }

        log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }

    /**
     * 从文件加载违禁词列表
     * 
     * @param filePath 文件路径（相对于classpath）
     * @return 违禁词列表
     */
    private static List<String> loadProhibitedWordsFromFile(String filePath) {
        try {
            Resource resource = new ClassPathResource(filePath);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            List<String> words = reader.lines()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .collect(Collectors.toList());

            log.info("从文件 {} 加载违禁词 {} 个", filePath, words.size());
            return words;
        } catch (IOException e) {
            log.error("加载违禁词文件 {} 失败", filePath, e);
            // 如果加载失败，返回一个默认的空列表
            return new ArrayList<>();
        }
    }

    /**
     * 编译正则表达式
     */
    private void compilePatterns() {
        this.compiledPatterns = new ArrayList<>();
        for (String word : prohibitedWords) {
            compiledPatterns.add(Pattern.compile(word, Pattern.CASE_INSENSITIVE));
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return -100; // 确保在其他Advisor之前执行
    }

    /**
     * 检查请求中是否包含违禁词
     */
    private AdvisedRequest before(AdvisedRequest request) {
        if (checkUserInput) {
            String userText = request.userText();
            if (containsProhibitedWord(userText)) {
                log.warn("检测到违禁词在用户输入中: {}", userText);
                throw new ProhibitedWordException("用户输入包含违禁词");
            }
        }
        return request;
    }

    /**
     * 检查响应中是否包含违禁词
     */
    private void observeAfter(AdvisedResponse advisedResponse) {
        if (checkAiResponse) {
            String responseText = advisedResponse.response().getResult().getOutput().getText();
            if (containsProhibitedWord(responseText)) {
                log.warn("检测到违禁词在AI响应中: {}", responseText);
                // 这里可以选择替换或拦截响应
            }
        }
    }

    /**
     * 检查文本中是否包含违禁词
     */
    private boolean containsProhibitedWord(String text) {
        if (!StringUtils.hasText(text)) {
            return false;
        }

        if (useRegex && compiledPatterns != null) {
            // 使用正则表达式匹配
            for (Pattern pattern : compiledPatterns) {
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    return true;
                }
            }
        } else {
            // 使用简单字符串匹配
            for (String word : prohibitedWords) {
                if (text.toLowerCase().contains(word.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        observeAfter(advisedResponse);
        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);

        if (checkAiResponse) {
            return new MessageAggregator().aggregateAdvisedResponse(advisedResponses, this::observeAfter);
        } else {
            return advisedResponses;
        }
    }

    /**
     * 违禁词异常
     */
    public static class ProhibitedWordException extends RuntimeException {
        public ProhibitedWordException(String message) {
            super(message);
        }
    }
}