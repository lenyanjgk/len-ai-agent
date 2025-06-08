package com.lenyan.lenaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QuizAssistantTest {

    @Resource
    private QuizAssistant quizAssistant;

    @Test
    public void run() {
        String userPrompt = """
                我刚完成了一个AI-MBTI测试，以下是测试结果总结：📋 测试基本信息
                测试名称：协调型实践者
                测试分数：90分
                测试类型：测评类
                测试时间：2025-06-09 04:47:40
                评分策略：AI
                📝 测试结果详情根据用户的回答，呈现出一种协调型实践者的性格特征。用户倾向于在社交场合中较为主动（B），在安排活动和做决定时也表现出较强的组织能力（B，B），这显示了其实践者的特质。同时，用户在面对规则（A）和新的挑战（A）时，表现出一种尊重传统但同时愿意尝试新事物的态度，这表明其在保持稳定性的同时也能够适应变化。在日常生活中的选择（B）和对待时间的方式（B）上，用户偏向于灵活和实际，注重日常安排的有序性，遇到问题时能够实际地解决问题（B）。总的来说，协调型实践者是一个既能够与人和谐相处，又能够有效管理日常生活和工作的人。
                ✍️ 我的答题记录选择的答案：A, B, A, B, A, B, B, B, B, B
                💡 请帮我详细分析这个测试结果，并给我一些具体的建议：
                输出的时候用中文。
                并且结合一些相关图片完成。
                """ ;
        String answer = quizAssistant.run(userPrompt);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

}
