package com.lenyan.lenaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "lenyanjgk.pdf";
        String content = "GitHub https://github.com/lenyanjgk";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}