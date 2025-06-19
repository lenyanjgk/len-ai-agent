package com.lenyan.lenaiagent.controller;

import com.lenyan.lenaiagent.constant.FileConstant;
import com.lenyan.lenaiagent.tools.PDFGenerationTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * FileResourceController 测试类
 * 测试PDF文件生成和下载功能
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FileResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private PDFGenerationTool pdfGenerationTool;
    private Path pdfDir;

    @BeforeEach
    public void setUp() {
        pdfGenerationTool = new PDFGenerationTool();
        pdfDir = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf");
        
        // 确保测试目录存在
        File dir = pdfDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Test
    public void testGenerateAndDownloadPDF() throws Exception {
        // 1. 使用PDFGenerationTool生成PDF
        String fileName = "test-file-" + System.currentTimeMillis() + ".pdf";
        String content = "# PDF测试标题\n\n这是一个测试PDF文件，用于测试文件下载功能。\n\n## 二级标题\n\n测试内容段落。";
        
        String result = pdfGenerationTool.generatePDF(fileName, content);
        System.out.println("生成PDF结果: " + result);
        
        // 确认PDF文件已生成
        Path pdfPath = pdfDir.resolve(fileName);
        assertTrue(Files.exists(pdfPath), "PDF文件应该已经被创建");
        assertTrue(Files.size(pdfPath) > 0, "PDF文件不应该为空");
        
        // 2. 测试获取PDF列表
        MvcResult listResult = mockMvc.perform(get("/api/files/pdf/list")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String listContent = listResult.getResponse().getContentAsString();
        System.out.println("PDF列表响应: " + listContent);
        assertTrue(listContent.contains(fileName), "PDF列表应包含新生成的文件");
        
        // 3. 测试下载PDF文件
        MvcResult downloadResult = mockMvc.perform(get("/api/files/pdf/" + fileName)
                .accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"" + fileName + "\""))
                .andReturn();
        
        MockHttpServletResponse response = downloadResult.getResponse();
        byte[] pdfContent = response.getContentAsByteArray();
        
        assertTrue(pdfContent.length > 0, "下载的PDF内容不应为空");
        assertEquals(Files.size(pdfPath), pdfContent.length, "下载的PDF大小应与原文件相同");
    }

    @Test
    public void testNonExistentFile() throws Exception {
        // 测试下载不存在的文件
        mockMvc.perform(get("/api/files/pdf/non-existent-file.pdf"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testInvalidPathTraversal() throws Exception {
        // 测试路径遍历攻击防御
        mockMvc.perform(get("/api/files/pdf/../../../sensitive-file.txt"))
                .andExpect(status().isBadRequest());
    }
} 