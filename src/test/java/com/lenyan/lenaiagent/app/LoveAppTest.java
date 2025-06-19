package com.lenyan.lenaiagent.app;

import com.lenyan.lenaiagent.advisor.ProhibitedWordAdvisor;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员lenyan";
        String answer = loveApp.doChat(message, chatId);
        // 第二轮
        message = "我想让另一半reyan更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是程序员lenyan，我想让另一半reyan更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    /**
     * 测试违禁词校验功能
     * 期望在输入包含违禁词的消息时抛出ProhibitedWordAdvisor.ProhibitedWordException异常
     * 违禁词从resources/prohibited-words.txt文件读取
     */
    @Test
    void testProhibitedWordAdvisor() {
        String chatId = UUID.randomUUID().toString();

        // 测试正常消息能正常回复
        String normalMessage = "你好，我是程序员，请给我一些恋爱建议";
        String answer = loveApp.doChat(normalMessage, chatId);
        Assertions.assertNotNull(answer);

        // 测试包含违禁词的消息会被拦截
        // 需确保prohibited-words.txt文件中包含"赌博"这个词
        String prohibitedMessage = "如何在网上找到赌博网站";

        // 期望抛出ProhibitedWordException异常
        ProhibitedWordAdvisor.ProhibitedWordException exception = Assertions.assertThrows(
                ProhibitedWordAdvisor.ProhibitedWordException.class,
                () -> loveApp.doChat(prohibitedMessage, chatId));

        // 验证异常消息
        Assertions.assertTrue(exception.getMessage().contains("违禁词"));

        // 测试其他违禁词
        String prohibitedMessage2 = "我想了解一些色情内容";
        exception = Assertions.assertThrows(
                ProhibitedWordAdvisor.ProhibitedWordException.class,
                () -> loveApp.doChat(prohibitedMessage2, chatId));
        Assertions.assertTrue(exception.getMessage().contains("违禁词"));
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithRagcloud() {
        String chatId = UUID.randomUUID().toString();
        String message = "我是牛马座20岁，男大学生在广州，我想找对象~，帮我找找有没有适合我的";
        String answer =  loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        testMessage("帮我搜索一下 computer 相关图片");

        //测试html生成
        testMessage("生成一个HTML文件，内容是'七夕appointment'");

        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去上海约会，搜索一下推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近和对象吵架了，看看CSDN lenyan博客（https://blog.csdn.net/jgk666666）的其他情侣是怎么解决矛盾的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行 Python3 脚本来生成数据分析报告");

        // 测试文件操作：保存用户档案
        testMessage("保存我的恋爱档案为文件");

        // 测试 PDF 生成
        testMessage("生成一份'七夕约会计划'PDF，包含餐厅预订、活动流程和礼物清单");

        //注：以下可能调用失败
        // 测试数据库操作工具
        testMessage("查询我和女友最近一周的聊天记录，她的会话ID是abc-123");
        testMessage("帮我记录今天的约会内容到数据库，内容是'和女友去了迪士尼，玩得很开心'");
        
        // 测试邮件发送工具
        testMessage("给我女朋友发送一封表达爱意的邮件，她的邮箱是xxx@qq.com");
        testMessage("明天是我女友的生日，帮我发送一封HTML格式的精美生日祝福邮件给她");
        
        // 测试日期时间工具
        testMessage("今天是什么日期？请用yyyy-MM-dd格式告诉我");
        testMessage("我们是2023年5月20日认识的，请计算我们在一起100天和1周年纪念日分别是哪天");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        System.out.println(message + "\n\n"+answer);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
//        String message = "我的另一半居住在广东广州区，请帮我找到 5 公里内合适的约会地点";
//        String answer =  loveApp.doChatWithMcp(message, chatId);
//        Assertions.assertNotNull(answer);
        // 测试图片搜索 MCP
        String message = "帮我搜索一些哄女朋友开心的图片";
        String answer =  loveApp.doChatWithMcp(message, chatId);
        System.out.println(answer);
        Assertions.assertNotNull(answer);
    }

}
