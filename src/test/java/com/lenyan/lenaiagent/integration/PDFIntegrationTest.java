package com.lenyan.lenaiagent.integration;

import com.lenyan.lenaiagent.constant.FileConstant;
import com.lenyan.lenaiagent.tools.PDFGenerationTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PDF生成和下载功能的集成测试
 * 测试PDF生成工具和文件资源控制器的集成功能
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PDFIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private PDFGenerationTool pdfGenerationTool;
    private File testFile;
    private String testFileName;

    @BeforeEach
    public void setUp() {
        pdfGenerationTool = new PDFGenerationTool();
        
        // 创建测试文件名
        testFileName = "integration-test-" + System.currentTimeMillis() + ".pdf";
        Path pdfDir = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf");
        testFile = pdfDir.resolve(testFileName).toFile();
        
        // 确保PDF目录存在
        pdfDir.toFile().mkdirs();
    }

    @AfterEach
    public void cleanup() {
        // 测试完成后删除测试文件
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }

    /**
     * 端到端测试：
     * 1. 使用PDFGenerationTool生成PDF
     * 2. 验证生成结果中包含下载链接
     * 3. 通过FileResourceController访问文件列表
     * 4. 通过FileResourceController下载PDF文件
     */
    @Test
    public void testEndToEndPDFGenerationAndDownload() throws Exception {
        // 第一步：生成PDF
        String content = "# 集成测试PDF\n\n这是一个测试内容，用于验证PDF生成和下载的完整流程。\n\n## 测试章节\n\n这是测试章节的内容。";
        String result = pdfGenerationTool.generatePDF(testFileName, content);
        
        System.out.println("====== PDF生成结果 ======");
        System.out.println(result);
        System.out.println("========================");
        
        // 验证结果包含下载链接
        assertTrue(result.contains("下载链接:"));
        assertTrue(result.contains("/api/files/pdf/" + testFileName));
        
        // 验证文件已创建
        assertTrue(testFile.exists(), "PDF文件应该已创建");
        assertTrue(testFile.length() > 0, "PDF文件不应该为空");
        
        // 第二步：通过API获取PDF文件列表
        MvcResult listResult = mockMvc.perform(get("/api/files/pdf/list"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String listResponse = listResult.getResponse().getContentAsString();
        System.out.println("PDF列表API响应: " + listResponse);
        
        // 验证列表中包含我们的测试文件
        assertTrue(listResponse.contains(testFileName), "PDF列表应包含测试文件");
        
        // 第三步：通过API下载PDF文件
        MvcResult downloadResult = mockMvc.perform(get("/api/files/pdf/" + testFileName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();
        
        byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();
        System.out.println("下载的PDF大小: " + downloadedContent.length + " 字节");
        
        // 验证下载的内容与原文件相同
        byte[] originalContent = Files.readAllBytes(testFile.toPath());
        assertEquals(originalContent.length, downloadedContent.length, "下载的内容大小应与原文件相同");
        
        // 验证内容相同
        assertArrayEquals(originalContent, downloadedContent, "下载的内容应与原文件相同");
    }
    
    /**
     * 测试支持中文内容的PDF生成和下载
     */
    @Test
    public void testChinesePDFGenerationAndDownload() throws Exception {
        String chineseFileName = "中文测试-" + System.currentTimeMillis() + ".pdf";
        String chineseContent = "# 中文PDF测试\n\n这是一个包含中文内容的PDF测试文件。\n\n## 第二章节\n\n中文内容测试，确保编码正确。";
        
        // 生成中文PDF
        String result = pdfGenerationTool.generatePDF(chineseFileName, chineseContent);
        System.out.println("中文PDF生成结果: " + result);
        
        // 获取文件路径
        Path chinesePdfPath = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf", chineseFileName);
        File chineseFile = chinesePdfPath.toFile();
        
        try {
            // 验证文件已创建
            assertTrue(chineseFile.exists(), "中文PDF文件应该已创建");
            assertTrue(chineseFile.length() > 0, "中文PDF文件不应该为空");
            
            // 通过API下载中文PDF
            MvcResult downloadResult = mockMvc.perform(get("/api/files/pdf/" + chineseFileName))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andReturn();
            
            byte[] downloadedContent = downloadResult.getResponse().getContentAsByteArray();
            byte[] originalContent = Files.readAllBytes(chinesePdfPath);
            
            // 验证内容
            assertEquals(originalContent.length, downloadedContent.length, "下载的中文PDF大小应与原文件相同");
            assertArrayEquals(originalContent, downloadedContent, "下载的中文PDF内容应与原文件相同");
        } finally {
            // 清理测试文件
            if (chineseFile.exists()) {
                chineseFile.delete();
            }
        }
    }
    
    /**
     * 测试错误处理
     */
    @Test
    public void testErrorHandling() throws Exception {
        // 测试访问不存在的文件
        mockMvc.perform(get("/api/files/pdf/non-existent.pdf"))
                .andExpect(status().isNotFound());
        
        // 测试路径遍历攻击
        mockMvc.perform(get("/api/files/pdf/../../../pom.xml"))
                .andExpect(status().isBadRequest());
        
        // 测试非PDF文件
        String txtFileName = "test.txt";
        try {
            // 创建一个txt文件
            Path txtPath = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf", txtFileName);
            Files.write(txtPath, "This is a text file".getBytes());
            
            // 尝试通过PDF接口访问它
            mockMvc.perform(get("/api/files/pdf/" + txtFileName))
                    .andExpect(status().isBadRequest());
        } catch (IOException e) {
            fail("测试准备阶段失败: " + e.getMessage());
        } finally {
            // 清理测试文件
            Path txtPath = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf", txtFileName);
            Files.deleteIfExists(txtPath);
        }
    }
} 