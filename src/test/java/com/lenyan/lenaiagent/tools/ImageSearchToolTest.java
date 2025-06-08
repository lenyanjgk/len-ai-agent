package com.lenyan.lenaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ImageSearchToolTest {

    @Autowired
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImage() {
        String query = "5公里内 广州 约会 地点";
        String result = imageSearchTool.searchImage(query);
        
        // 即使API返回错误，也不应抛出异常
        Assertions.assertNotNull(result);
        System.out.println("搜索结果: " + result);
    }
}
