package com.lenyan.lenaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.lenyan.lenaiagent.constant.FileConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML文件生成工具
 * 用于生成带有动效的HTML网页
 */
@Component
public class HtmlGenerationTool {

    private static final Logger log = LoggerFactory.getLogger(HtmlGenerationTool.class);
    
    // HTML文件保存目录
    private final String HTML_DIR = FileConstant.FILE_SAVE_DIR + "/html";
    
    public HtmlGenerationTool() {
        // 确保目录存在
        FileUtil.mkdir(HTML_DIR);
    }
    
    /**
     * 生成美观的HTML文件，包含动效和响应式设计
     * 注意：正文内容不应包含标题(h1)，因为标题将根据title参数自动生成
     * 
     * @param title HTML文档标题，将显示在页面顶部
     * @param content HTML正文内容(不应包含h1标题，因为标题会自动添加)
     * @param filename 可选的文件名(不含扩展名)，如果为空则自动生成
     * @return 结果信息，包含文件下载路径
     */
    @Tool(description = "Generate an attractive HTML file with animations and responsive design")
    public String generateHtml(
            @ToolParam(description = "The title of the HTML document") String title,
            @ToolParam(description = "The HTML content (body part, should NOT include the title)") String content,
            @ToolParam(description = "Optional filename (without extension), will generate if empty") String filename
    ) {
        try {
            // 处理文件名
            String safeFilename = getSafeFilename(filename);
            String htmlFilename = safeFilename + ".html";
            Path htmlPath = Paths.get(HTML_DIR, htmlFilename);
            
            // 生成完整的HTML内容
            String htmlContent = buildHtmlDocument(title, content);
            
            // 写入文件
            FileUtil.writeString(htmlContent, htmlPath.toString(), StandardCharsets.UTF_8);
            
            log.info("HTML文件生成成功: {}", htmlPath);
            
            // 构建下载链接
            String downloadLink = "/api/files/html/" + htmlFilename;
            
            return String.format(
                "HTML生成成功！\n" +
                "- 文件名: %s\n" +
                "- 本地路径: %s\n" +
                "- 下载链接: [点击查看HTML页面](%s)\n",
                htmlFilename, htmlPath.toString(), downloadLink
            );
            
        } catch (Exception e) {
            log.error("HTML生成失败", e);
            return "HTML生成失败: " + e.getMessage();
        }
    }
    
    /**
     * 确保文件名安全且有效
     */
    private String getSafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            // 如果没有提供文件名，生成一个基于时间戳和UUID的文件名
            return "html_" + System.currentTimeMillis() + "_" + 
                   UUID.randomUUID().toString().substring(0, 8);
        }
        
        // 移除不安全字符
        return filename.trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_") // 替换Windows/Unix不允许的字符
                .replaceAll("\\s+", "_");           // 替换空白字符
    }
    
    /**
     * 构建美观的HTML文档，包含动效
     * 
     * @param title 页面标题，将显示在h1标签中
     * @param bodyContent 页面正文内容(不应包含h1标题)
     * @return 完整的HTML文档字符串
     */
    private String buildHtmlDocument(String title, String bodyContent) {
        // 检查并移除bodyContent中可能存在的相同标题，防止重复显示
        String processedContent = bodyContent;
        
        if (title != null && !title.trim().isEmpty()) {
            // 获取标题的纯文本形式（去除前后空格）
            String plainTitle = title.trim();
            
            try {
                // 使用更安全的方式移除h1标签中的重复标题
                // (?i) - 不区分大小写匹配
                // (?s) - 允许.匹配换行符
                // <h1[^>]*> - 匹配h1开始标签及其属性
                // ([\\s\\S]*?) - 懒惰匹配任何内容，包括换行
                // </h1> - 匹配h1结束标签
                Pattern h1Pattern = Pattern.compile("(?i)(?s)<h1[^>]*>([\\s\\S]*?)</h1>");
                Matcher matcher = h1Pattern.matcher(processedContent);
                
                StringBuilder resultContent = new StringBuilder();
                int lastEnd = 0;
                
                while (matcher.find()) {
                    // 获取h1标签中的内容
                    String h1Content = matcher.group(1).trim();
                    
                    // 检查h1内容是否与title相似
                    // 移除HTML标签和多余空白字符来进行比较
                    String cleanedH1 = h1Content.replaceAll("<[^>]+>", "").trim();
                    String cleanedTitle = plainTitle.replaceAll("<[^>]+>", "").trim();
                    
                    // 如果h1内容与title相似则移除整个h1标签
                    if (cleanedH1.equalsIgnoreCase(cleanedTitle) || 
                        similarText(cleanedH1, cleanedTitle)) {
                        resultContent.append(processedContent.substring(lastEnd, matcher.start()));
                    } else {
                        // 保留不匹配的h1标签
                        resultContent.append(processedContent.substring(lastEnd, matcher.end()));
                    }
                    lastEnd = matcher.end();
                }
                
                // 添加剩余内容
                if (lastEnd < processedContent.length()) {
                    resultContent.append(processedContent.substring(lastEnd));
                }
                
                processedContent = resultContent.toString();
            } catch (Exception e) {
                // 如果正则处理出错，使用原始内容
                log.warn("移除HTML标题时出错: {}", e.getMessage());
            }
        }

        return "<!DOCTYPE html>\n" +
               "<html lang=\"zh-CN\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>" + escapeHtml(title) + "</title>\n" +
               "    <style>\n" +
               "        /* CSS变量定义（便于主题切换） */\n" +
               "        :root {\n" +
               "            --primary-color: #3498db;\n" +
               "            --secondary-color: #2c3e50;\n" +
               "            --accent-color: #e74c3c;\n" +
               "            --bg-color: #f8f9fa;\n" +
               "            --text-color: #333;\n" +
               "            --card-bg: #ffffff;\n" +
               "            --border-color: #ddd;\n" +
               "            --shadow-color: rgba(0,0,0,0.1);\n" +
               "            --animation-duration: 1.2s;\n" +
               "            --animation-delay: 0.1s;\n" +
               "        }\n" +
               "        /* 暗色模式主题 */\n" +
               "        [data-theme=\"dark\"] {\n" +
               "            --primary-color: #61dafb;\n" +
               "            --secondary-color: #f1f1f1;\n" +
               "            --accent-color: #ff6b6b;\n" +
               "            --bg-color: #121212;\n" +
               "            --text-color: #e0e0e0;\n" +
               "            --card-bg: #1e1e1e;\n" +
               "            --border-color: #444;\n" +
               "            --shadow-color: rgba(255,255,255,0.05);\n" +
               "        }\n" +
               "        /* 基础样式 */\n" +
               "        body { \n" +
               "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
               "            line-height: 1.8;\n" +
               "            color: var(--text-color);\n" +
               "            background-color: var(--bg-color);\n" +
               "            padding-top: 30px;\n" +
               "            opacity: 1;\n" +
               "            transition: background-color 0.3s ease, color 0.3s ease;\n" +
               "            scroll-behavior: smooth;\n" +
               "        }\n" +
               "        /* 页面加载器 */\n" +
               "        .page-loader {\n" +
               "            position: fixed;\n" +
               "            top: 0;\n" +
               "            left: 0;\n" +
               "            width: 100%;\n" +
               "            height: 100%;\n" +
               "            background: var(--bg-color);\n" +
               "            display: flex;\n" +
               "            justify-content: center;\n" +
               "            align-items: center;\n" +
               "            z-index: 9999;\n" +
               "            transition: opacity 0.5s ease, visibility 0.5s ease;\n" +
               "        }\n" +
               "        .loader-spinner {\n" +
               "            width: 50px;\n" +
               "            height: 50px;\n" +
               "            border: 5px solid rgba(0, 0, 0, 0.1);\n" +
               "            border-radius: 50%;\n" +
               "            border-top-color: var(--primary-color);\n" +
               "            animation: spin 1s linear infinite;\n" +
               "        }\n" +
               "        @keyframes spin {\n" +
               "            0% { transform: rotate(0deg); }\n" +
               "            100% { transform: rotate(360deg); }\n" +
               "        }\n" +
               "        /* 主容器 */\n" +
               "        .container {\n" +
               "            max-width: 1000px;\n" +
               "            background-color: var(--card-bg);\n" +
               "            padding: 30px;\n" +
               "            border-radius: 12px;\n" +
               "            box-shadow: 0 5px 15px var(--shadow-color);\n" +
               "            margin: 0 auto 30px auto;\n" +
               "            transition: background-color 0.3s ease, box-shadow 0.3s ease;\n" +
               "        }\n" +
               "        /* 主题切换器 */\n" +
               "        .theme-toggle {\n" +
               "            position: fixed;\n" +
               "            top: 20px;\n" +
               "            right: 20px;\n" +
               "            padding: 10px;\n" +
               "            background-color: var(--card-bg);\n" +
               "            border-radius: 50%;\n" +
               "            box-shadow: 0 2px 5px var(--shadow-color);\n" +
               "            cursor: pointer;\n" +
               "            z-index: 999;\n" +
               "            display: flex;\n" +
               "            justify-content: center;\n" +
               "            align-items: center;\n" +
               "            width: 40px;\n" +
               "            height: 40px;\n" +
               "            transition: all 0.3s ease;\n" +
               "        }\n" +
               "        .theme-toggle:hover {\n" +
               "            transform: scale(1.1);\n" +
               "        }\n" +
               "        /* 排版样式 */\n" +
               "        h1 {\n" +
               "            color: var(--secondary-color);\n" +
               "            font-weight: 700;\n" +
               "            margin-bottom: 30px;\n" +
               "            border-bottom: 2px solid var(--primary-color);\n" +
               "            padding-bottom: 15px;\n" +
               "            text-align: center;\n" +
               "            transition: color 0.3s ease;\n" +
               "        }\n" +
               "        h2 {\n" +
               "            color: var(--primary-color);\n" +
               "            margin-top: 40px;\n" +
               "            font-weight: 600;\n" +
               "            transition: color 0.3s ease;\n" +
               "        }\n" +
               "        h3 {\n" +
               "            color: var(--secondary-color);\n" +
               "            margin-top: 25px;\n" +
               "            transition: color 0.3s ease;\n" +
               "        }\n" +
               "        p {\n" +
               "            margin-bottom: 20px;\n" +
               "            font-size: 1.1rem;\n" +
               "        }\n" +
               "        /* 图片样式 */\n" +
               "        img {\n" +
               "            max-width: 100%;\n" +
               "            border-radius: 8px;\n" +
               "            box-shadow: 0 3px 10px var(--shadow-color);\n" +
               "            margin: 20px 0;\n" +
               "            opacity: 1;\n" +
               "            transition: transform 0.3s ease, box-shadow 0.3s ease;\n" +
               "        }\n" +
               "        img:hover {\n" +
               "            transform: scale(1.02);\n" +
               "            box-shadow: 0 5px 15px var(--shadow-color);\n" +
               "        }\n" +
               "        /* 实现懒加载的占位效果 */\n" +
               "        .lazy-load {\n" +
               "            transition: opacity 0.3s ease;\n" +
               "            opacity: 0;\n" +
               "        }\n" +
               "        .lazy-load.loaded {\n" +
               "            opacity: 1;\n" +
               "        }\n" +
               "        /* 列表样式 */\n" +
               "        ul, ol {\n" +
               "            margin-bottom: 20px;\n" +
               "            padding-left: 20px;\n" +
               "        }\n" +
               "        li {\n" +
               "            margin-bottom: 10px;\n" +
               "        }\n" +
               "        /* 卡片样式 */\n" +
               "        .card {\n" +
               "            background-color: var(--card-bg);\n" +
               "            border: 1px solid var(--border-color);\n" +
               "            border-radius: 8px;\n" +
               "            box-shadow: 0 3px 10px var(--shadow-color);\n" +
               "            margin-bottom: 30px;\n" +
               "            padding: 20px;\n" +
               "            transition: transform 0.3s ease, box-shadow 0.3s ease, background-color 0.3s ease;\n" +
               "        }\n" +
               "        .card:hover {\n" +
               "            transform: translateY(-5px);\n" +
               "            box-shadow: 0 8px 15px var(--shadow-color);\n" +
               "        }\n" +
               "        .card-title {\n" +
               "            color: var(--secondary-color);\n" +
               "            font-weight: 600;\n" +
               "            margin-bottom: 15px;\n" +
               "            transition: color 0.3s ease;\n" +
               "        }\n" +
               "        .card-img-top {\n" +
               "            width: 100%;\n" +
               "            border-top-left-radius: 8px;\n" +
               "            border-top-right-radius: 8px;\n" +
               "            margin-top: 0;\n" +
               "        }\n" +
               "        /* 特殊元素样式 */\n" +
               "        .highlight {\n" +
               "            background-color: var(--accent-color);\n" +
               "            padding: 2px 5px;\n" +
               "            border-radius: 3px;\n" +
               "            color: #fff;\n" +
               "            font-weight: 500;\n" +
               "        }\n" +
               "        blockquote {\n" +
               "            border-left: 4px solid var(--primary-color);\n" +
               "            padding: 10px 20px;\n" +
               "            color: var(--text-color);\n" +
               "            font-style: italic;\n" +
               "            margin: 20px 0;\n" +
               "            background-color: rgba(0,0,0,0.03);\n" +
               "            border-radius: 0 8px 8px 0;\n" +
               "            transition: background-color 0.3s ease;\n" +
               "        }\n" +
               "        [data-theme=\"dark\"] blockquote {\n" +
               "            background-color: rgba(255,255,255,0.05);\n" +
               "        }\n" +
               "        /* 代码块样式 */\n" +
               "        pre {\n" +
               "            background-color: #282c34;\n" +
               "            border-radius: 8px;\n" +
               "            padding: 15px;\n" +
               "            overflow-x: auto;\n" +
               "            margin: 20px 0;\n" +
               "            position: relative;\n" +
               "        }\n" +
               "        pre code {\n" +
               "            color: #abb2bf;\n" +
               "            font-family: 'Courier New', Courier, monospace;\n" +
               "            display: block;\n" +
               "            line-height: 1.5;\n" +
               "        }\n" +
               "        code {\n" +
               "            background-color: rgba(0,0,0,0.05);\n" +
               "            padding: 2px 5px;\n" +
               "            border-radius: 3px;\n" +
               "            font-family: 'Courier New', Courier, monospace;\n" +
               "            transition: background-color 0.3s ease;\n" +
               "        }\n" +
               "        [data-theme=\"dark\"] code:not(pre code) {\n" +
               "            background-color: rgba(255,255,255,0.1);\n" +
               "        }\n" +
               "        /* 表格样式 */\n" +
               "        table {\n" +
               "            width: 100%;\n" +
               "            border-collapse: collapse;\n" +
               "            margin: 20px 0;\n" +
               "            overflow-x: auto;\n" +
               "            box-shadow: 0 2px 8px var(--shadow-color);\n" +
               "            border-radius: 8px;\n" +
               "        }\n" +
               "        thead {\n" +
               "            background-color: var(--primary-color);\n" +
               "            color: white;\n" +
               "        }\n" +
               "        th, td {\n" +
               "            padding: 12px 15px;\n" +
               "            text-align: left;\n" +
               "            border-bottom: 1px solid var(--border-color);\n" +
               "        }\n" +
               "        tbody tr {\n" +
               "            transition: background-color 0.3s ease;\n" +
               "        }\n" +
               "        tbody tr:hover {\n" +
               "            background-color: rgba(0,0,0,0.03);\n" +
               "        }\n" +
               "        [data-theme=\"dark\"] tbody tr:hover {\n" +
               "            background-color: rgba(255,255,255,0.05);\n" +
               "        }\n" +
               "        /* 网格系统与响应式设计 */\n" +
               "        .row {\n" +
               "            display: flex;\n" +
               "            flex-wrap: wrap;\n" +
               "            margin: 0 -15px;\n" +
               "        }\n" +
               "        [class*='col-'] {\n" +
               "            padding: 0 15px;\n" +
               "            width: 100%;\n" +
               "        }\n" +
               "        .col-12 { width: 100%; }\n" +
               "        .col-6 { width: 100%; }\n" +
               "        .col-4 { width: 100%; }\n" +
               "        .col-3 { width: 100%; }\n" +
               "        \n" +
               "        @media (min-width: 768px) {\n" +
               "            .col-md-6 { width: 50%; }\n" +
               "            .col-md-4 { width: 33.333333%; }\n" +
               "            .col-md-3 { width: 25%; }\n" +
               "        }\n" +
               "        @media (min-width: 992px) {\n" +
               "            .col-lg-8 { width: 66.666667%; }\n" +
               "            .col-lg-6 { width: 50%; }\n" +
               "            .col-lg-4 { width: 33.333333%; }\n" +
               "            .col-lg-3 { width: 25%; }\n" +
               "        }\n" +
               "        /* 辅助样式类 */\n" +
               "        .card-body { padding: 15px; }\n" +
               "        .my-5 { margin-top: 3rem; margin-bottom: 3rem; }\n" +
               "        .my-4 { margin-top: 2rem; margin-bottom: 2rem; }\n" +
               "        .my-3 { margin-top: 1rem; margin-bottom: 1rem; }\n" +
               "        .mt-5 { margin-top: 3rem; }\n" +
               "        .mt-4 { margin-top: 2rem; }\n" +
               "        .mt-3 { margin-top: 1rem; }\n" +
               "        .mb-5 { margin-bottom: 3rem; }\n" +
               "        .mb-4 { margin-bottom: 2rem; }\n" +
               "        .mb-3 { margin-bottom: 1rem; }\n" +
               "        .p-5 { padding: 3rem; }\n" +
               "        .p-4 { padding: 2rem; }\n" +
               "        .p-3 { padding: 1rem; }\n" +
               "        .img-fluid { max-width: 100%; height: auto; }\n" +
               "        .rounded { border-radius: 8px; }\n" +
               "        .text-center { text-align: center; }\n" +
               "        .text-right { text-align: right; }\n" +
               "        .lead { font-size: 1.25rem; font-weight: 300; }\n" +
               "        .font-weight-bold { font-weight: 700; }\n" +
               "        .bg-light { background-color: var(--bg-color) !important; }\n" +
               "        .d-flex { display: flex; }\n" +
               "        .align-items-center { align-items: center; }\n" +
               "        .justify-content-between { justify-content: space-between; }\n" +
               "        .justify-content-center { justify-content: center; }\n" +
               "        .flex-column { flex-direction: column; }\n" +
               "        .w-100 { width: 100%; }\n" +
               "        .position-relative { position: relative; }\n" +
               "        .overflow-hidden { overflow: hidden; }\n" +
               "        /* 媒体内容容器 */\n" +
               "        .media-container {\n" +
               "            position: relative;\n" +
               "            padding-bottom: 56.25%; /* 16:9比例 */\n" +
               "            height: 0;\n" +
               "            overflow: hidden;\n" +
               "            margin: 20px 0;\n" +
               "            border-radius: 8px;\n" +
               "            box-shadow: 0 3px 10px var(--shadow-color);\n" +
               "        }\n" +
               "        .media-container iframe,\n" +
               "        .media-container video {\n" +
               "            position: absolute;\n" +
               "            top: 0;\n" +
               "            left: 0;\n" +
               "            width: 100%;\n" +
               "            height: 100%;\n" +
               "            border-radius: 8px;\n" +
               "        }\n" +
               "        /* 打印样式 */\n" +
               "        @media print {\n" +
               "            body {\n" +
               "                color: #000;\n" +
               "                background: #fff;\n" +
               "            }\n" +
               "            .container {\n" +
               "                max-width: 100%;\n" +
               "                box-shadow: none;\n" +
               "                padding: 0;\n" +
               "            }\n" +
               "            .theme-toggle, .page-loader {\n" +
               "                display: none !important;\n" +
               "            }\n" +
               "            img {\n" +
               "                max-width: 500px;\n" +
               "            }\n" +
               "            a {\n" +
               "                text-decoration: underline;\n" +
               "                color: #000;\n" +
               "            }\n" +
               "            a[href]:after {\n" +
               "                content: \" (\" attr(href) \")\";\n" +
               "                font-size: 0.8em;\n" +
               "            }\n" +
               "        }\n" +
               "        /* 动画类 */\n" +
               "        .fade-in {\n" +
               "            animation: fadeIn var(--animation-duration) ease forwards;\n" +
               "            opacity: 0;\n" +
               "        }\n" +
               "        @keyframes fadeIn {\n" +
               "            from { opacity: 0; }\n" +
               "            to { opacity: 1; }\n" +
               "        }\n" +
               "        .slide-up {\n" +
               "            animation: slideUp var(--animation-duration) ease forwards;\n" +
               "            opacity: 0;\n" +
               "        }\n" +
               "        @keyframes slideUp {\n" +
               "            from { transform: translateY(30px); opacity: 0; }\n" +
               "            to { transform: translateY(0); opacity: 1; }\n" +
               "        }\n" +
               "        .slide-left {\n" +
               "            animation: slideLeft var(--animation-duration) ease forwards;\n" +
               "            opacity: 0;\n" +
               "        }\n" +
               "        @keyframes slideLeft {\n" +
               "            from { transform: translateX(30px); opacity: 0; }\n" +
               "            to { transform: translateX(0); opacity: 1; }\n" +
               "        }\n" +
               "        /* 优化动画触发 */\n" +
               "        .animate-on-scroll {\n" +
               "            opacity: 0;\n" +
               "            transition: opacity 0.8s ease, transform 0.8s ease;\n" +
               "        }\n" +
               "        .animate-on-scroll.animated {\n" +
               "            opacity: 1;\n" +
               "        }\n" +
               "        .delay-100 { animation-delay: 0.1s; }\n" +
               "        .delay-200 { animation-delay: 0.2s; }\n" +
               "        .delay-300 { animation-delay: 0.3s; }\n" +
               "        .delay-400 { animation-delay: 0.4s; }\n" +
               "        .delay-500 { animation-delay: 0.5s; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <!-- 页面加载器 -->\n" +
               "    <div class=\"page-loader\" id=\"pageLoader\">\n" +
               "        <div class=\"loader-spinner\"></div>\n" +
               "    </div>\n" +
               "\n" +
               "    <!-- 主题切换按钮 -->\n" +
               "    <div class=\"theme-toggle\" id=\"themeToggle\" title=\"切换明暗主题\">\n" +
               "        <svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\">\n" +
               "            <path d=\"M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z\"></path>\n" +
               "        </svg>\n" +
               "    </div>\n" +
               "\n" +
               "    <!-- 内容容器 -->\n" +
               "    <div class=\"container fade-in\">\n" +
               "        <h1 class=\"slide-up\">" + escapeHtml(title) + "</h1>\n" +
               "        <div class=\"content\">\n" +
               processedContent + "\n" +
               "        </div>\n" +
               "    </div>\n" +
               "\n" +
               "    <script>\n" +
               "        // 页面加载完成后执行\n" +
               "        document.addEventListener('DOMContentLoaded', function() {\n" +
               "            // 主题切换功能\n" +
               "            const themeToggle = document.getElementById('themeToggle');\n" +
               "            const prefersDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches;\n" +
               "            \n" +
               "            // 检查用户上次的主题偏好\n" +
               "            const savedTheme = localStorage.getItem('theme');\n" +
               "            if (savedTheme === 'dark' || (!savedTheme && prefersDarkMode)) {\n" +
               "                document.documentElement.setAttribute('data-theme', 'dark');\n" +
               "            }\n" +
               "            \n" +
               "            // 设置主题切换事件\n" +
               "            themeToggle.addEventListener('click', function() {\n" +
               "                const currentTheme = document.documentElement.getAttribute('data-theme');\n" +
               "                if (currentTheme === 'dark') {\n" +
               "                    document.documentElement.removeAttribute('data-theme');\n" +
               "                    localStorage.setItem('theme', 'light');\n" +
               "                } else {\n" +
               "                    document.documentElement.setAttribute('data-theme', 'dark');\n" +
               "                    localStorage.setItem('theme', 'dark');\n" +
               "                }\n" +
               "            });\n" +
               "\n" +
               "            // 优化页面加载处理\n" +
               "            setTimeout(function() {\n" +
               "                document.getElementById('pageLoader').style.opacity = '0';\n" +
               "                setTimeout(() => {\n" +
               "                    document.getElementById('pageLoader').style.display = 'none';\n" +
               "                }, 500);\n" +
               "            }, 800);\n" +
               "\n" +
               "            // 图片加载处理\n" +
               "            const images = document.querySelectorAll('img');\n" +
               "            let loadedImages = 0;\n" +
               "            const totalImages = images.length;\n" +
               "\n" +
               "            function hidePageLoader() {\n" +
               "                document.getElementById('pageLoader').style.opacity = '0';\n" +
               "                setTimeout(() => {\n" +
               "                    document.getElementById('pageLoader').style.display = 'none';\n" +
               "                }, 500);\n" +
               "            }\n" +
               "\n" +
               "            // 如果没有图片，直接隐藏加载器\n" +
               "            if (totalImages === 0) {\n" +
               "                hidePageLoader();\n" +
               "            } else {\n" +
               "                // 处理图片加载\n" +
               "                images.forEach(function(img) {\n" +
               "                    // 添加懒加载类\n" +
               "                    img.classList.add('lazy-load');\n" +
               "                    \n" +
               "                    // 图片已加载完成\n" +
               "                    if (img.complete) {\n" +
               "                        img.classList.add('loaded');\n" +
               "                        imageLoaded();\n" +
               "                    } else {\n" +
               "                        // 监听加载事件\n" +
               "                        img.addEventListener('load', function() {\n" +
               "                            img.classList.add('loaded');\n" +
               "                            imageLoaded();\n" +
               "                        });\n" +
               "                        img.addEventListener('error', function() {\n" +
               "                            img.classList.add('loaded');\n" +
               "                            imageLoaded();\n" +
               "                        });\n" +
               "                    }\n" +
               "                });\n" +
               "            }\n" +
               "\n" +
               "            function imageLoaded() {\n" +
               "                loadedImages++;\n" +
               "                if (loadedImages >= totalImages) {\n" +
               "                    hidePageLoader();\n" +
               "                }\n" +
               "            }\n" +
               "\n" +
               "            // 动画效果优化 - 逐步显示元素\n" +
               "            const animatedElements = document.querySelectorAll('h2, h3, p, img, ul, ol, .card, pre, table, blockquote');\n" +
               "            \n" +
               "            // 添加滚动监听以触发动画\n" +
               "            const observer = new IntersectionObserver((entries) => {\n" +
               "                entries.forEach(entry => {\n" +
               "                    if (entry.isIntersecting) {\n" +
               "                        entry.target.classList.add('animated');\n" +
               "                        observer.unobserve(entry.target);\n" +
               "                    }\n" +
               "                });\n" +
               "            }, { threshold: 0.1 });\n" +
               "\n" +
               "            // 为每个元素添加动画类和延迟\n" +
               "            animatedElements.forEach(function(el, index) {\n" +
               "                // 添加基础类\n" +
               "                el.classList.add('animate-on-scroll');\n" +
               "                \n" +
               "                // 根据元素类型添加不同动画类型\n" +
               "                if (el.tagName === 'H2' || el.tagName === 'H3') {\n" +
               "                    el.style.transform = 'translateY(20px)';\n" +
               "                } else if (el.tagName === 'IMG') {\n" +
               "                    el.style.transform = 'scale(0.95)';\n" +
               "                } else if (el.classList.contains('card')) {\n" +
               "                    el.style.transform = 'translateY(20px)';\n" +
               "                } else {\n" +
               "                    el.style.transform = 'translateY(10px)';\n" +
               "                }\n" +
               "                \n" +
               "                // 监听元素\n" +
               "                observer.observe(el);\n" +
               "            });\n" +
               "\n" +
               "            // 当元素进入视口时，添加过渡动画\n" +
               "            document.querySelectorAll('.animate-on-scroll.animated').forEach(el => {\n" +
               "                el.style.transform = 'none';\n" +
               "            });\n" +
               "            \n" +
               "            // 处理错误图片\n" +
               "            document.querySelectorAll('img').forEach(function(img) {\n" +
               "                img.addEventListener('error', function() {\n" +
               "                    // 图片加载失败时显示占位符\n" +
               "                    img.style.minHeight = '200px';\n" +
               "                    img.style.background = 'rgba(0,0,0,0.1)';\n" +
               "                    img.style.display = 'flex';\n" +
               "                    img.style.alignItems = 'center';\n" +
               "                    img.style.justifyContent = 'center';\n" +
               "                    img.alt = '图片加载失败';\n" +
               "                });\n" +
               "            });\n" +
               "            \n" +
               "            // 处理表格响应式\n" +
               "            document.querySelectorAll('table').forEach(function(table) {\n" +
               "                const wrapper = document.createElement('div');\n" +
               "                wrapper.style.overflowX = 'auto';\n" +
               "                table.parentNode.insertBefore(wrapper, table);\n" +
               "                wrapper.appendChild(table);\n" +
               "            });\n" +
               "        });\n" +
               "    </script>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * 简单的HTML转义
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * 比较两个字符串的相似度
     * 
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果字符串非常相似则返回true
     */
    private boolean similarText(String str1, String str2) {
        if (str1 == null || str2 == null) return false;
        
        // 规范化字符串（去除多余空白字符）
        String normalized1 = str1.replaceAll("\\s+", " ").trim().toLowerCase();
        String normalized2 = str2.replaceAll("\\s+", " ").trim().toLowerCase();
        
        // 如果完全相同，直接返回true
        if (normalized1.equals(normalized2)) return true;
        
        // 计算字符串长度
        int len1 = normalized1.length();
        int len2 = normalized2.length();
        
        // 如果长度差异太大，则认为是不同的标题
        if (Math.abs(len1 - len2) > Math.max(len1, len2) * 0.2) {
            return false;
        }
        
        // 计算Levenshtein距离
        int distance = levenshteinDistance(normalized1, normalized2);
        
        // 计算相似度阈值，较长的字符串允许更多差异
        int threshold = Math.min(3, Math.max(len1, len2) / 10);
        
        // 如果差异小于阈值，认为是相似的
        return distance <= threshold;
    }

    /**
     * 计算两个字符串之间的Levenshtein距离
     * 
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 两个字符串之间的Levenshtein距离
     */
    private int levenshteinDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (str1.charAt(i - 1) == str2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }

        return dp[len1][len2];
    }
} 