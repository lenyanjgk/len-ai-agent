package com.lenyan.lenaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Image;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.properties.TextAlignment;
import com.lenyan.lenaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF生成工具
 */
public class PDFGenerationTool {

    private static final Logger log = LoggerFactory.getLogger(PDFGenerationTool.class);

    // Markdown标题正则
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");
    
    // Markdown图片正则
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[(.*?)\\]\\((.*?)\\)");

    /**
     * 生成PDF文件
     */
    @Tool(description = "Generate a PDF file with given content and images, supports markdown image syntax ![](url) and headers with # symbols", returnDirect = false)
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF") String fileName,
            @ToolParam(description = "Content to be included in the PDF, supports markdown image syntax ![](url) and headers with # symbols") String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        
        // 确保文件名以.pdf结尾
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            filePath += ".pdf";
        }
        
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            
            // 加载字体（提前检查字体可用性）
            PdfFont font;
            boolean chineseFontLoaded = false;
            try {
                font = loadFont();
                chineseFontLoaded = true;
                log.info("成功加载中文字体");
            } catch (Exception e) {
                log.error("加载中文字体失败，将使用默认字体", e);
                font = PdfFontFactory.createFont();
            }
            
            // 创建PDF
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                
                document.setFont(font);
                
                // 如果内容为空，添加默认内容
                if (content == null || content.trim().isEmpty()) {
                    Paragraph emptyWarning = new Paragraph("未提供内容，PDF已创建但内容为空。");
                    emptyWarning.setFont(font).setFontSize(14);
                    document.add(emptyWarning);
                } else {
                    // 检查中文字体是否正确加载，如果没有加载，给出提示
                    if (!chineseFontLoaded) {
                        Paragraph fontWarning = new Paragraph("注意：无法加载中文字体，文档中的中文可能无法正确显示。");
                        fontWarning.setFont(font).setFontSize(14);
                        document.add(fontWarning);
                        document.add(new Paragraph("\n"));
                    }
                    
                    // 处理内容
                    processContent(content, document, font);
                }
            }
            
            // 检查生成的PDF文件
            if (FileUtil.exist(filePath) && FileUtil.size(new java.io.File(filePath)) > 0) {
                // 创建下载链接
                String pdfFileName = new java.io.File(filePath).getName();
                String downloadUrl = "/api/files/pdf/" + pdfFileName;
                
                StringBuilder result = new StringBuilder();
                result.append("PDF生成成功！\n");
                result.append("- 文件名: ").append(pdfFileName).append("\n");
                result.append("- 本地路径: ").append(filePath).append("\n");
                result.append("- 下载链接: [点击下载PDF](").append(downloadUrl).append(")\n");
                
                if (!chineseFontLoaded) {
                    result.append("\n⚠️ 注意：未能加载中文字体，PDF中的中文可能无法正确显示。");
                }
                
                return result.toString();
            } else {
                return "PDF文件创建失败或为空文件: " + filePath;
            }
            
        } catch (IOException e) {
            log.error("生成PDF时出错", e);
            return "生成PDF时出错: " + e.getMessage() + "\n请检查是否需要安装中文字体，或将中文字体文件(.ttc/.ttf)复制到resources/fonts/目录下。";
        } catch (Exception e) {
            log.error("PDF生成过程中发生未知错误", e);
            return "PDF生成过程中发生未知错误: " + e.getMessage();
        }
    }
    
    /**
     * 加载字体，支持多种加载方式和回退机制
     * 
     * 注意：要完全支持中文，需要将中文字体文件（如simsun.ttc、simhei.ttf等）
     * 复制到项目的src/main/resources/fonts/目录下
     * 
     * Windows中文字体位置通常在：C:/Windows/Fonts/
     * Linux中文字体通常在：/usr/share/fonts/
     * Mac中文字体通常在：/Library/Fonts/ 或 ~/Library/Fonts/
     */
    private PdfFont loadFont() throws IOException {
        PdfFont font;
        try {
            // 尝试方式1：从资源目录加载字体（需要在resources目录中放置字体文件）
            String[] fontNames = {"simsun.ttc", "simhei.ttf", "msyh.ttc", "simkai.ttf"};
            
            // 尝试多种编码
            String[] encodings = {"Identity-H", "UTF-8"};
            
            for (String fontName : fontNames) {
                try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream("fonts/" + fontName)) {
                    if (fontStream != null) {
                        byte[] fontBytes = new byte[fontStream.available()];
                        fontStream.read(fontBytes);
                        
                        // 尝试使用不同的编码
                        for (String encoding : encodings) {
                            try {
                                font = PdfFontFactory.createFont(fontBytes, encoding);
                                log.info("加载嵌入式字体成功: {} 使用编码: {}", fontName, encoding);
                                return font;
                            } catch (Exception e) {
                                log.debug("使用编码 {} 加载字体 {} 失败: {}", encoding, fontName, e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("无法加载嵌入式字体: " + fontName, e);
                }
            }
            
            // 尝试方式2：使用常见的系统中文字体名称 (Windows)
            String[] windowsFontPaths = {
                "C:/Windows/Fonts/simsun.ttc", 
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simfang.ttf",
                "C:/Windows/Fonts/simli.ttf"
            };
            
            for (String fontPath : windowsFontPaths) {
                for (String encoding : encodings) {
                    try {
                        font = PdfFontFactory.createFont(fontPath, encoding);
                        log.info("加载Windows系统字体成功: {} 使用编码: {}", fontPath, encoding);
                        return font;
                    } catch (Exception e) {
                        log.debug("使用编码 {} 加载字体 {} 失败: {}", encoding, fontPath, e.getMessage());
                    }
                }
            }
            
            // 尝试方式3：使用系统字体并尝试添加索引
            for (String fontPath : windowsFontPaths) {
                for (int index = 0; index < 3; index++) {
                    String indexedPath = fontPath + "," + index;
                    for (String encoding : encodings) {
                        try {
                            font = PdfFontFactory.createFont(indexedPath, encoding);
                            log.info("使用索引加载Windows系统字体成功: {} 使用编码: {}", indexedPath, encoding);
                            return font;
                        } catch (Exception e) {
                            // 不记录日志，以避免过多输出
                        }
                    }
                }
            }
            
            // 尝试方式4：使用Linux/Mac系统中文字体路径
            String[] unixFontPaths = {
                "/usr/share/fonts/truetype/arphic/uming.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/Library/Fonts/Arial Unicode.ttf",
                "/System/Library/Fonts/STHeiti Light.ttc",
                "/System/Library/Fonts/PingFang.ttc"
            };
            
            for (String fontPath : unixFontPaths) {
                for (String encoding : encodings) {
                    try {
                        font = PdfFontFactory.createFont(fontPath, encoding);
                        log.info("加载Unix系统字体成功: {} 使用编码: {}", fontPath, encoding);
                        return font;
                    } catch (Exception e) {
                        log.debug("使用编码 {} 加载字体 {} 失败: {}", encoding, fontPath, e.getMessage());
                    }
                }
            }
            
            // 尝试方式5：尝试使用iText内置的中文字体
            try {
                font = PdfFontFactory.createFont("STSong-Light", "Identity-H");
                log.info("使用iText内置中文字体STSong-Light成功");
                return font;
            } catch (Exception e) {
                log.warn("无法使用iText内置中文字体", e);
            }
            
            // 尝试方式6：尝试使用中文别名
            String[] chineseFontAliases = {
                "SimSun", "宋体", "黑体", "微软雅黑", "楷体", 
                "FangSong", "KaiTi", "SimHei", "Microsoft YaHei"
            };
            
            for (String fontName : chineseFontAliases) {
                try {
                    font = PdfFontFactory.createFont(fontName, "Identity-H");
                    log.info("使用中文字体别名成功: {}", fontName);
                    return font;
                } catch (Exception e) {
                    log.debug("使用中文字体别名 {} 失败", fontName);
                }
            }
            
            // 回退方案：使用iText的标准字体（不支持中文，但至少能生成PDF）
            log.warn("所有中文字体加载尝试均失败，使用默认字体（不支持中文）");
            return PdfFontFactory.createFont();
            
        } catch (Exception e) {
            log.error("所有字体加载方式均失败", e);
            // 最终回退：使用内置字体
            return PdfFontFactory.createFont();
        }
    }
    
    /**
     * 处理内容
     */
    private void processContent(String content, Document document, PdfFont font) {
        String[] lines = content.split("\n");
        StringBuilder textBuffer = new StringBuilder();
        
        for (String line : lines) {
            Matcher headerMatcher = HEADER_PATTERN.matcher(line);
            if (headerMatcher.matches()) {
                if (textBuffer.length() > 0) {
                    processTextWithImages(textBuffer.toString(), document);
                    textBuffer.setLength(0);
                }
                
                String headerMarker = headerMatcher.group(1);
                String headerText = headerMatcher.group(2);
                addHeader(document, headerText, headerMarker.length(), font);
                continue;
            }
            
            textBuffer.append(line).append("\n");
        }
        
        if (textBuffer.length() > 0) {
            processTextWithImages(textBuffer.toString(), document);
        }
    }
    
    /**
     * 添加标题
     */
    private void addHeader(Document document, String headerText, int level, PdfFont font) {
        Paragraph header = new Paragraph(headerText);
        
        float fontSize = 24f;
        switch (level) {
            case 1: fontSize = 24f; break;
            case 2: fontSize = 20f; break;
            case 3: fontSize = 18f; break;
            case 4: fontSize = 16f; break;
            case 5: fontSize = 14f; break;
            case 6: fontSize = 12f; break;
        }
        
        header.setFont(font)
              .setFontSize(fontSize)
              .setTextAlignment(TextAlignment.LEFT);
        
        document.add(header);
    }
    
    /**
     * 处理包含图片的文本
     */
    private void processTextWithImages(String content, Document document) {
        Matcher matcher = IMAGE_PATTERN.matcher(content);
        
        int lastEnd = 0;
        
        while (matcher.find()) {
            String textBefore = content.substring(lastEnd, matcher.start());
            if (!textBefore.isEmpty()) {
                document.add(new Paragraph(textBefore));
            }
            
            String imageUrl = matcher.group(2);
            try {
                Image image = new Image(ImageDataFactory.create(new URL(imageUrl)));
                image.setWidth(document.getPdfDocument().getDefaultPageSize().getWidth() * 0.8f);
                image.setAutoScale(true);
                document.add(image);
            } catch (Exception e) {
                document.add(new Paragraph("无法加载图片: " + imageUrl + " (" + e.getMessage() + ")"));
            }
            
            lastEnd = matcher.end();
        }
        
        if (lastEnd < content.length()) {
            document.add(new Paragraph(content.substring(lastEnd)));
        }
    }
}
