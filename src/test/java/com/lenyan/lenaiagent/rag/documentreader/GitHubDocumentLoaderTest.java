package com.lenyan.lenaiagent.rag.documentreader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.HttpConnector;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

@SpringBootTest
class GitHubDocumentLoaderTest {
    
    @Value("${documentreader.token.github}")
    private String githubtoken;

    // 信任所有证书的HttpConnector(仅用于测试)
    private static class TrustAllCertsConnector implements HttpConnector {
        private final SSLContext sslContext;
        private final HostnameVerifier allHostsValid;

        public TrustAllCertsConnector() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                };
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new SecureRandom());
                allHostsValid = (hostname, session) -> true;
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                throw new RuntimeException("初始化TrustAllCertsConnector失败", e);
            }
        }

        @Override
        public HttpURLConnection connect(URL url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                httpsConnection.setHostnameVerifier(allHostsValid);
            }
            return connection;
        }
    }

    private GitHub createGitHubClientIgnoringCertificates(String token) throws IOException {
        return new GitHubBuilder()
                .withOAuthToken(token)
                .withConnector(new TrustAllCertsConnector())
                .build();
    }

    @Test
    public void testGitHubDocumentLoader() throws IOException {
        GitHub github = createGitHubClientIgnoringCertificates(githubtoken);
        github.checkApiUrlValidity();
        
        GitHubDocumentLoader loader = GitHubDocumentLoader.builder()
                .gitHub(github)
                .owner("lenyanjgk")
                .repo("len-ai-agent")
                .branch("master")
                .build();
        
        // 测试获取仓库信息
        Map<String, Object> repoInfo = loader.getRepositoryInfo();
        Assertions.assertNotNull(repoInfo, "仓库信息不应为空");
        
        // 测试加载单个文件
        Document doc = loader.loadDocument("/README.md");
        System.out.println("readme.md加载" + doc);
        Assertions.assertNotNull(doc, "文档不应为空");
        Assertions.assertNotNull(doc.getId(), "文档ID不应为空");
        Assertions.assertNotNull(doc.getText(), "文档文本不应为空");
        Assertions.assertFalse(doc.getText().isEmpty(), "文档文本不应为空");
        
        // 测试加载目录
        List<Document> docs = loader.loadDocuments("/");
        System.out.println("目录的加载" + docs);
        Assertions.assertNotNull(docs, "文档列表不应为空");
    }
}