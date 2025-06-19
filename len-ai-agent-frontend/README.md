# AI 聊天应用

这是一个使用 Vue 3 开发的前端项目，包含两个 AI 聊天应用：AI 恋爱大师和 AI 超级智能体。

## 项目结构

```
len-ai-agent-frontend/
├── public/                  # 静态资源
├── src/                     # 源代码
│   ├── assets/              # 资源文件
│   ├── components/          # 组件
│   │   ├── ChatMessage.vue  # 聊天消息组件
│   │   └── ChatInterface.vue # 聊天界面组件
│   ├── views/               # 视图页面
│   │   ├── HomeView.vue     # 主页
│   │   ├── LoveAppView.vue  # AI 恋爱大师页面
│   │   └── ManusAppView.vue # AI 超级智能体页面
│   ├── router/              # 路由配置
│   ├── services/            # API 服务
│   ├── utils/               # 工具函数
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
└── package.json             # 依赖配置
```

## 功能特点

1. 主页用于切换不同的AI应用
2. AI 恋爱大师应用：与恋爱顾问AI聊天
3. AI 超级智能体应用：与通用AI聊天
4. 实时聊天功能：使用SSE技术实现
5. 聊天历史保存：使用localStorage存储聊天记录

## 后端API

应用连接到本地运行的SpringBoot后端：

- 基础URL：`http://localhost:8102/api`
- AI 恋爱大师API：`/ai/love_app/chat/sse`
- AI 超级智能体API：`/ai/manus/chat`

## 开发设置

安装依赖：
```
npm install
```

启动开发服务器：
```
npm run serve
```

构建生产版本：
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
