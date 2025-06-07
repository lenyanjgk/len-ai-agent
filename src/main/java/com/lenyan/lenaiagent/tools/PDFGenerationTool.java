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

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF生成工具
 */
public class PDFGenerationTool {

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
        
        try {
            FileUtil.mkdir(fileDir);
            
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);
                
                processContent(content, document, font);
                
            }
            return "PDF生成成功，保存路径: " + filePath;
        } catch (IOException e) {
            return "生成PDF时出错: " + e.getMessage();
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
