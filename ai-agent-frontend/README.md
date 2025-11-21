# AI Agent Frontend

Vue3 + TypeScript + Axios 前端项目

## 功能特性

1. **主页**：应用切换中心，可以选择不同的 AI 应用
2. **AI 恋爱大师**：参考 deepseek 界面设计，支持 SSE 流式对话
3. **AI 超级智能体**：展示 AI 的思考、行动、工具调用等过程

## 技术栈

- Vue 3
- TypeScript
- Vue Router
- Axios
- Vite

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动

## 构建生产版本

```bash
npm run build
```

## 项目结构

```
ai-agent-frontend/
├── src/
│   ├── api/           # API 接口
│   ├── router/        # 路由配置
│   ├── utils/         # 工具类
│   ├── views/         # 页面组件
│   ├── App.vue        # 根组件
│   └── main.ts        # 入口文件
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 后端接口

- 接口前缀：`http://localhost:8123/api`
- AI 恋爱大师：`GET /api/ai/chat_app/chat/SseEmitter?userMessage=xxx&chatId=xxx`
- AI 超级智能体：`GET /api/ai/manus/chat?userMessage=xxx`

## 注意事项

1. 确保后端服务运行在 `http://localhost:8123`
2. 前端通过 Vite 代理转发 API 请求（配置在 `vite.config.ts`）
3. 对话历史功能暂未实现后端接口，chatId 由前端生成

