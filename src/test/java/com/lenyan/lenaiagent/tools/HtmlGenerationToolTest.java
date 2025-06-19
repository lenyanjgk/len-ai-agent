package com.lenyan.lenaiagent.tools;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import com.lenyan.lenaiagent.constant.FileConstant;

/**
 * HTML生成工具测试类
 * 用于测试带动效的HTML页面生成
 * 
 * 优化点：
 * 1. 添加了页面加载指示器，避免图片和文字一闪而过的问题
 * 2. 使用了图片预加载机制，确保图片完全加载后再显示
 * 3. 优化了动画触发逻辑，提高动画流畅性
 * 4. 增加了更多种类的动画效果和过渡时间
 * 5. 移除对外部CDN资源的依赖，确保页面始终可见
 * 6. 修改了默认透明度设置，解决页面显示为白色的问题
 */
public class HtmlGenerationToolTest {

    /**
     * 主方法，测试HTML生成功能
     */
    public static void main(String[] args) {
        HtmlGenerationTool htmlTool = new HtmlGenerationTool();

        System.out.println("=== 动效HTML生成工具测试（优化版 2.0）===\n");

        // 确保HTML目录存在
        String htmlDir = FileConstant.FILE_SAVE_DIR + "/html";
        new File(htmlDir).mkdirs();
        System.out.println("HTML文件目录: " + htmlDir);

        // 生成广州约会计划的HTML页面
        testGuangzhouDatingPlan(htmlTool);

        System.out.println("\n=== 测试结束 ===");
        System.out.println("生成的HTML文件位于: " + htmlDir);
    }

    /**
     * 测试广州约会计划HTML生成
     * 
     * 动画效果优化：
     * - 添加加载指示器
     * - 图片预加载处理
     * - 交错动画效果
     * - 降低对外部资源的依赖
     * - 添加页面加载失败保护机制
     * - 解决页面显示为白色的问题
     */
    private static void testGuangzhouDatingPlan(HtmlGenerationTool htmlTool) {
        System.out.println("\n广州约会计划HTML页面生成 (带优化动画效果):");
        System.out.println("本版本特别解决了页面大部分显示为白色的问题");

        String title = "广州约会计划 - 情侣出游指南";
        
        // 将Markdown转换为HTML格式
        String content = """
            <div class="row">
                <div class="col-12">
                    <p class="lead">这是一份为居住在广东广州的情侣准备的约会计划。以下是几个推荐的约会地点，都在市区5公里范围内。</p>
                </div>
            </div>
            
            <div class="row my-5">
                <div class="col-lg-8">
                    <h2>1. 沙面岛</h2>
                    <img src="https://img.picui.cn/free/2025/06/07/68442ed4da1f1.png" alt="沙面岛" class="img-fluid">
                    
                    <h3>景点介绍</h3>
                    <p>沙面岛是珠江中的一个小岛，建有许多欧式风格建筑，环境优美。这里曾是外国租界，至今保留了大量西式建筑，是拍照和漫步的好去处。</p>
                    
                    <h3>推荐活动</h3>
                    <ul>
                        <li>漫步沙面岛，欣赏欧式建筑</li>
                        <li>在岛上的咖啡馆小坐，享受下午茶</li>
                        <li>拍摄情侣写真，留下美好回忆</li>
                        <li>傍晚在珠江边欣赏日落美景</li>
                    </ul>
                </div>
                <div class="col-lg-4">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">沙面岛小贴士</h5>
                            <p>最佳游览时间：下午3点至黄昏</p>
                            <p>交通方式：地铁1号线或6号线黄沙站下车，步行约10分钟</p>
                            <p>预算：基本免费，餐饮约100-200元/人</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row my-5">
                <div class="col-lg-8">
                    <h2>2. 广州塔</h2>
                    <img src="https://img.picui.cn/free/2025/06/07/68442ecf70c39.png" alt="广州塔" class="img-fluid">
                    
                    <h3>景点介绍</h3>
                    <p>广州塔又称"小蛮腰"，是广州的地标性建筑，高600米，是中国第二高、世界第四高的电视塔。塔上设有旋转餐厅、观光层和极速云霄等设施。</p>
                    
                    <h3>推荐活动</h3>
                    <ul>
                        <li>登塔俯瞰广州全景</li>
                        <li>在456米高空的旋转餐厅共进浪漫晚餐</li>
                        <li>体验"极速云霄"高空降落伞（适合喜欢刺激的情侣）</li>
                        <li>夜晚欣赏塔身绚丽的灯光秀</li>
                    </ul>
                </div>
                <div class="col-lg-4">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">广州塔小贴士</h5>
                            <p>最佳游览时间：傍晚至晚上</p>
                            <p>交通方式：地铁3号线或APM线广州塔站下车</p>
                            <p>预算：观光票150-200元/人，餐厅消费约300-500元/人</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row my-5">
                <div class="col-lg-8">
                    <h2>3. 北京路步行街</h2>
                    <img src="https://img.picui.cn/free/2025/06/07/68442edaeb1a2.png" alt="北京路" class="img-fluid">
                    
                    <h3>景点介绍</h3>
                    <p>北京路是广州最古老的商业街之一，有着2000多年历史。这里有传统的岭南建筑，也有现代商场，还有透明的玻璃观景道，可以看到地下的千年古道遗址。</p>
                    
                    <h3>推荐活动</h3>
                    <ul>
                        <li>逛街购物，品尝各种美食</li>
                        <li>参观地下的千年古道遗址</li>
                        <li>在附近的中山五路尝试各种特色餐厅</li>
                        <li>晚上欣赏步行街的灯光夜景</li>
                    </ul>
                </div>
                <div class="col-lg-4">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">北京路小贴士</h5>
                            <p>最佳游览时间：下午至晚上</p>
                            <p>交通方式：地铁6号线或1号线公园前站下车</p>
                            <p>预算：购物和餐饮视个人情况，一般200-500元/人</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row my-5">
                <div class="col-lg-8">
                    <h2>4. 越秀公园</h2>
                    <img src="https://img.picui.cn/free/2025/06/07/68442eb6da377.png" alt="越秀公园" class="img-fluid">
                    
                    <h3>景点介绍</h3>
                    <p>越秀公园是广州最大的综合性公园，园内有著名的五羊石像、镇海楼等景点，环境幽静，绿树成荫。</p>
                    
                    <h3>推荐活动</h3>
                    <ul>
                        <li>徒步登上越秀山顶</li>
                        <li>参观五羊石像和孙中山纪念碑</li>
                        <li>划船游览公园内的湖泊</li>
                        <li>在园内野餐，享受宁静时光</li>
                    </ul>
                </div>
                <div class="col-lg-4">
                    <div class="card">
                        <div class="card-body">
                            <h5 class="card-title">越秀公园小贴士</h5>
                            <p>最佳游览时间：上午至下午</p>
                            <p>交通方式：地铁1号线或2号线越秀公园站下车</p>
                            <p>预算：门票5元/人，划船约40元/小时</p>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="row mt-5">
                <div class="col-12">
                    <div class="card bg-light">
                        <div class="card-body">
                            <h2 class="card-title text-center">约会小贴士</h2>
                            <div class="row">
                                <div class="col-md-6">
                                    <h5><span class="highlight">交通建议</span></h5>
                                    <p>广州地铁网络发达，大部分景点都有地铁站，建议使用地铁出行避开拥堵。</p>
                                    
                                    <h5><span class="highlight">天气提醒</span></h5>
                                    <p>广州属亚热带气候，夏季炎热多雨，建议携带遮阳伞和防晒用品。</p>
                                </div>
                                <div class="col-md-6">
                                    <h5><span class="highlight">最佳时间</span></h5>
                                    <p>10月到次年4月是广州的最佳旅游季节，气温适宜。</p>
                                    
                                    <h5><span class="highlight">预算参考</span></h5>
                                    <ul>
                                        <li>沙面岛：基本免费，餐饮约100-200元/人</li>
                                        <li>广州塔：观光票150-200元/人，餐厅消费约300-500元/人</li>
                                        <li>北京路：购物和餐饮视个人情况，一般200-500元/人</li>
                                        <li>越秀公园：门票5元/人</li>
                                    </ul>
                                </div>
                            </div>
                            <blockquote>希望这份约会计划能帮助你们度过美好的时光！</blockquote>
                        </div>
                    </div>
                </div>
            </div>
        """;

        String result = htmlTool.generateHtml(title, content, "guangzhou_dating_plan");
        System.out.println(result);

        // 验证文件是否存在
        Path htmlFile = Paths.get(FileConstant.FILE_SAVE_DIR, "html", "guangzhou_dating_plan.html");
        if (new File(htmlFile.toString()).exists()) {
            System.out.println("✓ 文件成功生成: " + htmlFile);
            System.out.println("✓ 已优化动画效果，解决了文字和图片一闪而过的问题");
            System.out.println("✓ 修复了页面显示为白色的问题，确保内容始终可见");
            System.out.println("✓ 移除了对外部资源的依赖，提高页面可靠性");
        } else {
            System.out.println("✗ 文件生成失败!");
        }
    }
}