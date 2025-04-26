# Len AI Agent - AI超级智能体

基于Spring AI和LangChain4j构建的智能AI交互系统，支持多种大模型接入、知识库问答和工具调用能力。

## 项目简介

Len AI Agent是一个以AI开发为核心的智能体项目，旨在提供强大的AI交互能力，包括：

- 多轮对话与上下文记忆
- 知识库问答 (RAG)
- 工具调用与智能规划
- MCP服务集成
- 多模态应用支持

项目集成了主流AI大模型，如阿里云DashScope的通义千问、Ollama本地部署模型等，同时支持多种AI框架，使开发者能够快速构建智能应用。

## 技术栈

- **基础框架**: Java 21 + Spring Boot 3.4.5
- **AI框架**: Spring AI + LangChain4j
- **AI模型接入**:
  - 阿里云DashScope (通义千问)
  - Ollama (本地部署模型)
- **工具与库**:
  - Knife4j (API文档)
  - Hutool (工具类库)
  - Lombok (代码简化)
- **服务配置**: 支持多环境配置 (local, dev, prod)

## 核心功能

### 1. 多种方式调用AI大模型

- Spring AI框架集成
- LangChain4j链式调用
- 原生SDK调用
- 自定义HTTP调用

### 2. 知识库问答 (RAG)

- 支持接入本地知识库
- 支持向量数据库检索
- 提供上下文增强查询

### 3. 智能工具调用

- 文件操作
- 联网搜索
- 网页抓取
- 资源下载
- PDF生成

### 4. MCP服务能力

- 自定义MCP服务开发
- 图片搜索服务
- 支持多种调用方式

### 5. 智能体规划能力

- 基于ReAct模式的自主规划
- 多步骤任务执行
- 任务状态追踪

## 项目结构

```
len-ai-agent/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── lenyan/
│   │   │           └── lenaiagent/
│   │   │               ├── controller/    # 接口控制器
│   │   │               ├── demo/          # 示例代码
│   │   │               │   └── invoke/    # 不同调用方式示例
│   │   │               ├── config/        # 配置类
│   │   │               ├── service/       # 服务实现
│   │   │               ├── model/         # 数据模型
│   │   │               └── LenAiAgentApplication.java  # 启动类
│   │   └── resources/
│   │       ├── application.yml            # 通用配置
│   │       └── application-local.yml      # 本地环境配置
│   └── test/                              # 测试代码
└── pom.xml                                # 项目依赖
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- IDE: IntelliJ IDEA (推荐)

### 本地运行

1. **克隆项目**

```bash
git clone https://github.com/yourusername/len-ai-agent.git
cd len-ai-agent
```

2. **配置API密钥**

修改 `src/main/resources/application-local.yml` 文件中的API密钥：

```yaml
spring:
  ai:
    dashscope:
      api-key: 你的DashScope API密钥
```

3. **本地大模型部署(可选)**

如需使用本地大模型，请安装并启动Ollama:

```bash
# 安装Ollama后，拉取模型
ollama pull deepseek-r1:7b
# 启动服务 (默认端口11434)
ollama serve
```

4. **启动应用**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

5. **访问API文档**

打开浏览器访问 [http://localhost:8102/api/swagger-ui.html](http://localhost:8102/api/swagger-ui.html)

## 示例代码

### Spring AI框架调用

```java
@Resource
private ChatModel dashscopeChatModel;

public String chat(String input) {
    AssistantMessage response = dashscopeChatModel.call(new Prompt(input))
        .getResult()
        .getOutput();
    return response.getText();
}
```

### LangChain4j调用

```java
ChatLanguageModel model = QwenChatModel.builder()
        .apiKey("your-api-key")
        .modelName("qwen-max")
        .build();
String answer = model.chat("你好，我想了解一下AI Agent");
```

## 扩展功能

- 对话记忆持久化
- 结构化输出
- 多模态支持
- 知识库扩展
- 自定义工具开发
- 智能体增强

## 许可证

本项目使用 [MIT 许可证](LICENSE)

## 贡献指南

欢迎提交Issue或Pull Request！

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建Pull Request

## 联系方式

- 项目维护者: [lenyan](https://blog.csdn.net/jgk666666)
- 项目讨论群: [xxx xxx]

---

*本项目是"lenAI超级智能体"系列的实现，旨在帮助开发者快速掌握AI开发技术，构建智能应用。* 
