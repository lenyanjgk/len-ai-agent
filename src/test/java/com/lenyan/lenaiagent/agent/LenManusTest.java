package com.lenyan.lenaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LenManusTest {

    @Resource
    private LenManus lenManus;

    @Test
    public void run() {
        String userPrompt = """
                我的女朋友居住在广东广州软件学院，请帮我找到 5 公里内合适的约会地点，
                并搜索合结合一些网络图片，制定一份详细的约会计划，
                并以 PDF 格式输出
                """ ;
        String answer = lenManus.run(userPrompt);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }
}