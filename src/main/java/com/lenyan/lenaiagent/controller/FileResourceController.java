package com.lenyan.lenaiagent.controller;

import com.lenyan.lenaiagent.constant.FileConstant;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件资源控制器
 * 用于提供各种类型文件的下载和列表功能
 * 支持PDF和HTML文件的内联显示和下载
 */
@RestController
@RequestMapping("/files")
public class FileResourceController {

    private static final Logger log = LoggerFactory.getLogger(FileResourceController.class);

    // 设置各种文件目录
    private final Path pdfDir = Paths.get(FileConstant.FILE_SAVE_DIR, "pdf");
    private final Path htmlDir = Paths.get(FileConstant.FILE_SAVE_DIR, "html");
    private final Path fileDir = Paths.get(FileConstant.FILE_SAVE_DIR, "file");
    private final Path generalDir = Paths.get(FileConstant.FILE_SAVE_DIR);

    /**
     * 获取PDF文件列表
     */
    @GetMapping("/pdf/list")
    public List<Map<String, Object>> listPdfFiles() throws IOException {
        log.info("正在获取PDF文件列表，目录: {}", pdfDir);
        return listFiles(pdfDir, "pdf");
    }

    /**
     * 下载PDF文件
     */
    @GetMapping("/pdf/{filename:.+}")
    public ResponseEntity<Resource> downloadPdfFile(@PathVariable String filename) {
        log.info("请求下载PDF文件: {}", filename);
        return downloadFile(pdfDir, filename, "pdf");
    }

    /**
     * 获取HTML文件列表
     */
    @GetMapping("/html/list")
    public List<Map<String, Object>> listHtmlFiles() throws IOException {
        log.info("正在获取HTML文件列表，目录: {}", htmlDir);
        return listFiles(htmlDir, "html");
    }

    /**
     * 下载或查看HTML文件
     * 默认以内联方式展示HTML内容
     */
    @GetMapping("/html/{filename:.+}")
    public ResponseEntity<Resource> downloadHtmlFile(@PathVariable String filename) {
        log.info("请求查看HTML文件: {}", filename);
        return downloadFile(htmlDir, filename, "html");
    }

    /**
     * 获取一般文件列表
     */
    @GetMapping("/file/list")
    public List<Map<String, Object>> listGeneralFiles() throws IOException {
        log.info("正在获取一般文件列表，目录: {}", fileDir);
        return listFiles(fileDir, "file");
    }

    /**
     * 下载一般文件
     */
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> downloadGeneralFile(@PathVariable String filename) {
        log.info("请求下载一般文件: {}", filename);
        return downloadFile(fileDir, filename, "file");
    }

    /**
     * 通用的文件列表方法
     */
    private List<Map<String, Object>> listFiles(Path directory, String type) throws IOException {
        // 确保目录存在
        File dir = directory.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("创建目录: {}", directory);
        }
        
        return Files.list(directory)
                .filter(Files::isRegularFile)
                .map(path -> {
                    Map<String, Object> fileInfo = new HashMap<>();
                    String fileName = path.getFileName().toString();
                    fileInfo.put("name", fileName);
                    fileInfo.put("url", "/api/files/" + type + "/" + fileName);
                    try {
                        fileInfo.put("size", Files.size(path));
                        fileInfo.put("lastModified", Files.getLastModifiedTime(path).toMillis());
                        
                        // 添加文件类型信息
                        String fileType = determineFileType(fileName);
                        fileInfo.put("type", fileType);
                        
                        // 添加显示方式，对于HTML和PDF是内联
                        fileInfo.put("display", shouldDisplayInline(fileType) ? "inline" : "download");
                    } catch (IOException e) {
                        log.warn("获取文件信息失败: {}", fileName, e);
                    }
                    return fileInfo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 通用的文件下载方法
     */
    private ResponseEntity<Resource> downloadFile(Path directory, String filename, String fileType) {
        try {
            Path filePath = directory.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            // 安全检查：确保文件确实在指定目录中
            if (!resource.exists()) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            if (!resource.isReadable()) {
                log.warn("文件不可读: {}", filePath);
                return ResponseEntity.badRequest().build();
            }
            
            if (!filePath.startsWith(directory.normalize())) {
                log.warn("安全错误：请求路径超出允许范围: {}", filePath);
                return ResponseEntity.badRequest().build();
            }

            // 检测文件类型
            String contentType = determineContentType(filePath, fileType);
            log.info("提供文件: {}, 类型: {}", filename, contentType);
            
            // 确定是否使用内联显示还是下载附件
            String disposition = shouldDisplayInline(contentType) ? 
                "inline" : "attachment";
            
            // 对于HTML文件，添加安全头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                        disposition + "; filename=\"" + resource.getFilename() + "\"");
                
            if (contentType.equals("text/html")) {
                // 添加安全相关头信息，允许从原始站点加载资源
                headers.add("X-Content-Type-Options", "nosniff");
                headers.add("X-Frame-Options", "SAMEORIGIN");
                headers.add("Content-Security-Policy", "default-src 'self'; img-src 'self' https://* data:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline';");
            }
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
                    
        } catch (MalformedURLException e) {
            log.error("URL格式错误: {}", filename, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据文件名确定文件类型
     */
    private String determineFileType(String fileName) {
        if (fileName.toLowerCase().endsWith(".pdf")) {
            return "pdf";
        } else if (fileName.toLowerCase().endsWith(".html") || fileName.toLowerCase().endsWith(".htm")) {
            return "html";
        } else if (fileName.toLowerCase().endsWith(".txt")) {
            return "text";
        } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || 
                   fileName.toLowerCase().endsWith(".png") || fileName.toLowerCase().endsWith(".gif")) {
            return "image";
        } else {
            return "other";
        }
    }
    
    /**
     * 确定文件的内容类型
     */
    private String determineContentType(Path filePath, String fileType) {
        // 首先尝试使用文件后缀名
        String fileName = filePath.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".txt")) {
            return "text/plain";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".xml")) {
            return "application/xml";
        } else if (fileName.endsWith(".csv")) {
            return "text/csv";
        }
        
        // 尝试使用Files.probeContentType
        try {
            String probed = Files.probeContentType(filePath);
            if (probed != null && !probed.isEmpty()) {
                return probed;
            }
        } catch (IOException e) {
            log.warn("无法确定文件类型: {}", filePath);
        }
        
        // 无法确定，返回通用二进制类型
        return "application/octet-stream";
    }
    
    /**
     * 判断是否应该内联显示还是作为附件下载
     */
    private boolean shouldDisplayInline(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.startsWith("image/") ||
               contentType.equals("text/html");
    }
} 