package com.lenyan.lenaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PDF生成工具类测试 - 测试Markdown和网络图片的组合
 */
class PDFGenerationToolTest {
    
    /**
     * 测试包含Markdown格式和网络图片的PDF生成
     */
    @Test
    void testMarkdownWithImages() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "guangzhou_dating_plan.pdf";
        
        // 使用Markdown标题语法和网络图片URL
        String content = """
                # 广州约会计划
                
                这是一份为居住在广东广州的情侣准备的约会计划。以下是几个推荐的约会地点，都在市区5公里范围内。
                
                ## 1. 沙面岛
                
                ![沙面岛](https://img.picui.cn/free/2025/06/07/68442ed4da1f1.png)
                
                ### 景点介绍
                
                沙面岛是珠江中的一个小岛，建有许多欧式风格建筑，环境优美。这里曾是外国租界，至今保留了大量西式建筑，是拍照和漫步的好去处。
                
                ### 推荐活动
                
                - 漫步沙面岛，欣赏欧式建筑
                - 在岛上的咖啡馆小坐，享受下午茶
                - 拍摄情侣写真，留下美好回忆
                - 傍晚在珠江边欣赏日落美景
                
                ## 2. 广州塔
                
                ![广州塔](https://img.picui.cn/free/2025/06/07/68442ecf70c39.png)
                
                ### 景点介绍
                
                广州塔又称"小蛮腰"，是广州的地标性建筑，高600米，是中国第二高、世界第四高的电视塔。塔上设有旋转餐厅、观光层和极速云霄等设施。
                
                ### 推荐活动
                
                - 登塔俯瞰广州全景
                - 在456米高空的旋转餐厅共进浪漫晚餐
                - 体验"极速云霄"高空降落伞（适合喜欢刺激的情侣）
                - 夜晚欣赏塔身绚丽的灯光秀
                
                ## 3. 北京路步行街
                
                ![北京路](https://img.picui.cn/free/2025/06/07/68442edaeb1a2.png)
                
                ### 景点介绍
                
                北京路是广州最古老的商业街之一，有着2000多年历史。这里有传统的岭南建筑，也有现代商场，还有透明的玻璃观景道，可以看到地下的千年古道遗址。
                
                ### 推荐活动
                
                - 逛街购物，品尝各种美食
                - 参观地下的千年古道遗址
                - 在附近的中山五路尝试各种特色餐厅
                - 晚上欣赏步行街的灯光夜景
                
                ## 4. 越秀公园
                
                ![越秀公园](https://img.picui.cn/free/2025/06/07/68442eb6da377.png)
                
                ### 景点介绍
                
                越秀公园是广州最大的综合性公园，园内有著名的五羊石像、镇海楼等景点，环境幽静，绿树成荫。
                
                ### 推荐活动
                
                - 徒步登上越秀山顶
                - 参观五羊石像和孙中山纪念碑
                - 划船游览公园内的湖泊
                - 在园内野餐，享受宁静时光
                
                ## 约会小贴士
                
                1. **交通建议**：广州地铁网络发达，大部分景点都有地铁站，建议使用地铁出行避开拥堵。
                2. **天气提醒**：广州属亚热带气候，夏季炎热多雨，建议携带遮阳伞和防晒用品。
                3. **最佳时间**：10月到次年4月是广州的最佳旅游季节，气温适宜。
                4. **预算参考**：
                   - 沙面岛：基本免费，餐饮约100-200元/人
                   - 广州塔：观光票150-200元/人，餐厅消费约300-500元/人
                   - 北京路：购物和餐饮视个人情况，一般200-500元/人
                   - 越秀公园：门票5元/人
                
                希望这份约会计划能帮助你们度过美好的时光！
                """;
        
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
        assertTrue(result.contains("PDF生成成功"));
        System.out.println(result);
    }
    
    /**
     * 主方法，允许直接运行测试
     */
    public static void main(String[] args) {
        PDFGenerationToolTest test = new PDFGenerationToolTest();
        
        System.out.println("=== 开始测试Markdown和网络图片 ===");
        test.testMarkdownWithImages();
        System.out.println("=== 测试完成 ===\n");
        
        System.out.println("PDF文件已生成在tmp/pdf目录下，请查看 guangzhou_dating_plan.pdf");
    }
}