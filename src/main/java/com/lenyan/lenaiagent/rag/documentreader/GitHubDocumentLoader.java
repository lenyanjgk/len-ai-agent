package com.lenyan.lenaiagent.rag.documentreader;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GitHub文档加载器
 * author: lenyan
 */
@Slf4j
public class GitHubDocumentLoader {

    private final GitHub gitHub;
    private final String owner;
    private final String repo;
    private final String branch;
    private String defaultBranch; // 缓存默认分支

    /**
     * 构造函数
     *
     * @param gitHub GitHub客户端实例
     * @param owner  仓库所有者
     * @param repo   仓库名称
     * @param branch 分支名称
     */
    public GitHubDocumentLoader(GitHub gitHub, String owner, String repo, String branch) {
        Assert.notNull(gitHub, "GitHub实例不能为空");
        Assert.notNull(owner, "仓库所有者不能为空");
        Assert.notNull(repo, "仓库名称不能为空");
        this.gitHub = gitHub;
        this.owner = owner;
        this.repo = repo;
        this.branch = branch != null ? branch : "main";
    }

    /**
     * 创建Builder实例
     *
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 加载单个文件内容
     *
     * @param path 文件路径
     * @return Document对象
     */
    public Document loadDocument(String path) {
        try {
            return Optional.ofNullable(normalizePath(path))
                    .flatMap(this::loadContentSafely)
                    .map(this::createDocumentSafely)
                    .orElseThrow(() -> new RuntimeException("加载文档失败: " + path));
        } catch (Exception e) {
            log.error("加载文档失败: {}", path);
            throw new RuntimeException("加载文档失败: " + path, e);
        }
    }

    /**
     * 加载目录下的所有文件
     *
     * @param path 目录路径
     * @return Document列表
     */
    public List<Document> loadDocuments(String path) {
        try {
            List<GHContent> contents = loadDirectoryContentWithBranchFallback(normalizePath(path));

            return contents.stream()
                    .flatMap(content -> {
                        if (content.isFile()) {
                            try {
                                return Stream.of(createDocument(content));
                            } catch (Exception e) {
                                return Stream.empty();
                            }
                        } else if (content.isDirectory()) {
                            return loadDocuments(content.getPath()).stream();
                        }
                        return Stream.empty();
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("加载目录失败: {}", path);
            throw new RuntimeException("加载目录失败: " + path, e);
        }
    }

    private Optional<GHContent> loadContentSafely(String path) {
        try {
            return Optional.of(loadContentWithBranchFallback(path));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Document createDocumentSafely(GHContent content) {
        try {
            return createDocument(content);
        } catch (IOException e) {
            throw new RuntimeException("创建文档失败", e);
        }
    }

    private GHContent loadContentWithBranchFallback(String path) throws IOException {
        try {
            return tryLoadContent(path, branch);
        } catch (GHFileNotFoundException e) {
            String defaultBranch = getDefaultBranch();
            if (!branch.equals(defaultBranch)) {
                return tryLoadContent(path, defaultBranch);
            }
            throw e;
        }
    }

    private GHContent tryLoadContent(String path, String branchName) throws IOException {
        GHContent content = getRepository().getFileContent(path, branchName);
        Assert.isTrue(content.isFile(), "路径必须指向文件");
        return content;
    }

    private List<GHContent> loadDirectoryContentWithBranchFallback(String path) throws IOException {
        try {
            return getRepository().getDirectoryContent(path.isEmpty() ? "/" : path, branch);
        } catch (GHFileNotFoundException e) {
            String defaultBranch = getDefaultBranch();
            if (!branch.equals(defaultBranch)) {
                return getRepository().getDirectoryContent(path.isEmpty() ? "/" : path, defaultBranch);
            }
            throw e;
        }
    }

    /**
     * 获取仓库的默认分支
     *
     * @return 默认分支名称
     * @throws IOException 如果获取失败
     */
    private String getDefaultBranch() throws IOException {
        return Optional.ofNullable(defaultBranch)
                .orElseGet(() -> {
                    try {
                        return defaultBranch = getRepository().getDefaultBranch();
                    } catch (IOException e) {
                        throw new RuntimeException("获取默认分支失败", e);
                    }
                });
    }

    /**
     * 规范化路径
     * 移除开头的斜杠，确保路径格式符合GitHub API要求
     *
     * @param path 原始路径
     * @return 规范化后的路径
     */
    private String normalizePath(String path) {
        return (path == null || path.isEmpty()) ? "" : path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * 获取仓库信息
     *
     * @return 仓库信息Map
     */
    public Map<String, Object> getRepositoryInfo() {
        try {
            GHRepository repository = getRepository();
            this.defaultBranch = repository.getDefaultBranch();

            return Map.of(
                    "name", Objects.toString(repository.getName(), ""),
                    "description", Objects.toString(repository.getDescription(), ""),
                    "stars", repository.getStargazersCount(),
                    "forks", repository.getForksCount(),
                    "language", Objects.toString(repository.getLanguage(), ""),
                    "defaultBranch", this.defaultBranch,
                    "htmlUrl", Optional.ofNullable(repository.getHtmlUrl()).map(Object::toString).orElse(""),
                    "cloneUrl", Objects.toString(repository.getHttpTransportUrl(), "")
            );
        } catch (IOException e) {
            log.error("获取仓库信息失败: {}/{}", owner, repo);
            throw new RuntimeException("获取仓库信息失败: " + owner + "/" + repo, e);
        }
    }

    /**
     * 获取仓库对象
     *
     * @return GHRepository对象
     * @throws IOException 如果获取失败
     */
    private GHRepository getRepository() throws IOException {
        return gitHub.getRepository(owner + "/" + repo);
    }

    /**
     * 创建Document对象
     *
     * @param content GitHub内容对象
     * @return Document对象
     * @throws IOException 如果读取失败
     */
    private Document createDocument(GHContent content) throws IOException {
        return new Document(
                content.getContent(),
                Map.of(
                        "github_file_name", content.getName(),
                        "github_file_path", content.getPath(),
                        "github_file_sha", content.getSha(),
                        "github_html_url", content.getHtmlUrl()
                )
        );
    }

    /**
     * 构建器类
     */
    public static class Builder {
        private GitHub gitHub;
        private String owner;
        private String repo;
        private String branch;

        /**
         * 设置GitHub客户端实例
         *
         * @param gitHub GitHub客户端实例
         * @return Builder实例
         */
        public Builder gitHub(GitHub gitHub) {
            this.gitHub = gitHub;
            return this;
        }

        /**
         * 设置仓库所有者
         *
         * @param owner 仓库所有者
         * @return Builder实例
         */
        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        /**
         * 设置仓库名称
         *
         * @param repo 仓库名称
         * @return Builder实例
         */
        public Builder repo(String repo) {
            this.repo = repo;
            return this;
        }

        /**
         * 设置分支名称
         *
         * @param branch 分支名称
         * @return Builder实例
         */
        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        /**
         * 构建GitHubDocumentLoader实例
         *
         * @return GitHubDocumentLoader实例
         */
        public GitHubDocumentLoader build() {
            return new GitHubDocumentLoader(gitHub, owner, repo, branch);
        }
    }
}
