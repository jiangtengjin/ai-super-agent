# AI Agent - 智能体助手平台

> 基于 Spring AI 构建的全功能 AI 智能体平台，集成了多模型接入、RAG 知识库、工具调用、会话管理等能力，内置"AI 恋爱大师"和"AI 超级智能体"两个应用。

## 项目简介

本项目是一个 AI 智能体开发平台，提供两种应用场景：

- **AI 恋爱大师**：基于 RAG 知识库的专业恋爱咨询助手，能够根据用户的情感状态（单身/恋爱/已婚）提供针对性建议
- **AI 超级智能体**：基于 ReAct 模式的通用智能体，具备自主思考、工具调用、多步骤任务执行能力

### 解决的问题

| 问题 | 解决方案 |
|------|----------|
| 大模型回答缺乏专业性 | RAG 检索增强，引入领域知识库 |
| 大模型无法执行实际操作 | 工具调用机制，支持文件操作、网页搜索等 |
| 单次对话无法完成复杂任务 | ReAct 模式，支持多步骤任务拆解 |
| 多模型切换成本高 | Spring AI 统一抽象，无缝切换 |
| 会话状态管理复杂 | 分布式 Session + 数据库持久化 |

## 核心功能

### AI 恋爱大师

- **情感状态识别**：自动识别用户当前情感状态（单身/恋爱/已婚）
- **知识库检索**：基于 RAG 技术检索恋爱知识库，提供专业建议
- **多轮对话**：支持上下文记忆，实现连续深入的情感咨询
- **结构化输出**：可将对话结果生成结构化的恋爱报告

### AI 超级智能体

- **ReAct 模式**：实现 Reasoning and Acting 循环，自主思考与行动
- **工具调用**：内置 9 种工具，AI 自动选择并组合使用
- **多步骤执行**：支持最多 20 步的复杂任务拆解
- **实时流式输出**：SSE 实时展示思考过程、工具选择、执行结果
- **会话中断**：支持主动中断正在进行的任务

### 内置工具

| 工具 | 功能 |
|------|------|
| WebSearchTool | 网络搜索（SerpAPI） |
| WebScrapTool | 网页内容爬取（Jsoup） |
| FileOperationTool | 文件读写 |
| PDFGenerationTool | PDF 文件生成（iText） |
| ResourceDownloadTool | 资源下载 |
| TerminalOperationTool | 终端命令执行 |
| QQEmailSenderTool | QQ 邮件发送 |
| TerminateTool | 任务终止 |
| AskHumanTool | 向人类寻求帮助 |

### 平台能力

- **用户系统**：邮箱注册、图片验证码、会话管理
- **会话管理**：对话记录持久化、游标分页查询、会话删除
- **安全防护**：敏感词过滤、Prompt 注入检测、分布式限流
- **MCP 协议**：支持通过 MCP 协议扩展外部工具服务

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.7 | 基础框架 |
| Spring AI | 1.0.0-M6 | AI 应用框架 |
| 阿里云千问（DashScope） | - | 大语言模型 |
| Ollama | - | 本地模型部署 |
| MyBatis-Plus | 3.5.14 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis + Redisson | 3.52.0 | 缓存与分布式锁 |
| PGVector | - | 向量数据库 |
| Spring Session | - | 分布式会话 |
| Knife4j | 4.4.0 | 接口文档 |
| iText | 9.1.0 | PDF 生成 |
| Jsoup | 1.19.1 | 网页爬取 |
| Hutool | 5.8.37 | 工具库 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3 | 前端框架 |
| TypeScript | 类型安全 |
| Vite | 构建工具 |
| Ant Design Vue | UI 组件库 |

## 项目亮点

### 1. 自研智能体框架

基于 ReAct 模式实现完整的智能体执行引擎，采用分层设计：

```
BaseAgent（状态机、内存管理、执行循环）
    └── ReActAgent（Think-Act 循环）
            └── ToolCallAgent（工具调用处理）
                    └── MyManus（具体实现）
```

### 2. 灵活的 Advisor 机制

通过 Spring AI 的 Advisor 机制实现可插拔的增强功能：

- **CustomLoggerAdvisor**：自定义日志记录
- **ReReadAdvisor**：重复读取提升推理能力（R2 技术）
- **UserMessageCheckAdvisor**：敏感词和 Prompt 注入检测

### 3. 完整的 RAG 链路

实现从文档加载到知识检索的完整流程：

```
文档加载 → 文档切分 → 关键词增强 → 向量化存储 → 查询优化 → 知识检索
```

支持多种向量存储方案：
- 本地 SimpleVectorStore
- PGVector 向量数据库
- 阿里云 DashScope 云知识库

### 4. 分布式限流

基于 Redisson 实现分布式限流，支持多种限流维度：

- **用户级**：每个用户独立限流
- **接口级**：每个接口独立限流
- **IP 级**：每个 IP 独立限流

### 5. 多模型统一接入

通过 Spring AI 的 `ChatModel` 抽象，实现多模型无缝切换：

- 阿里云千问（DashScope）
- 本地 Ollama 模型

## 快速开始

### 环境要求

- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+（前端）

### 后端启动

1. **克隆项目**
```bash
git clone https://github.com/jiangtengjin/ai-agent.git
cd ai-agent
```

2. **初始化数据库**
```bash
mysql -u root -p < sql/create_table.sql
```

3. **修改配置**

编辑 `src/main/resources/application-local.yml`：

```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key  # 阿里云千问 API Key
  datasource:
    url: jdbc:mysql://localhost:3306/ai_agent
    username: root
    password: your-password
  data:
    redis:
      host: localhost
      port: 6379
```

4. **启动后端**
```bash
./mvnw spring-boot:run
```

后端服务默认运行在 http://localhost:8123/api

### 前端启动

```bash
cd ai-agent-frontend
npm install
npm run dev
```

前端默认运行在 http://localhost:5173

## 接口文档

启动后端后访问：

- Knife4j：http://localhost:8123/api/doc.html
- Swagger UI：http://localhost:8123/api/swagger-ui.html

## 项目结构

```
ai-agent/
├── src/main/java/com/xhh/aiagent/
│   ├── advisor/                    # Advisor 增强
│   │   ├── CustomLoggerAdvisor     # 日志记录
│   │   ├── ReReadAdvisor           # 重复读取提升推理
│   │   └── UserMessageCheckAdvisor # 安全检测
│   ├── app/                        # 应用层
│   │   └── LoveApp                 # 恋爱大师应用
│   ├── chatmemory/                 # 对话记忆
│   │   ├── FileBasedChatMemory     # 文件持久化（Kryo）
│   │   └── InDBChatMemory          # 数据库持久化
│   ├── controller/                 # 控制器
│   │   ├── AiController            # AI 对话接口
│   │   ├── CaptchaController       # 验证码
│   │   ├── ChatHistoryController   # 对话历史
│   │   ├── CodeMailController      # 邮箱验证码
│   │   ├── ConversationController  # 会话管理
│   │   └── UserController          # 用户管理
│   ├── manus/                      # 智能体框架
│   │   ├── BaseAgent               # 基类（状态机、执行循环）
│   │   ├── ReActAgent              # ReAct 模式
│   │   ├── ToolCallAgent           # 工具调用
│   │   └── MyManus                 # 智能体实现
│   ├── rag/                        # RAG 实现
│   │   ├── documentreader/         # 文档读取器
│   │   ├── querytransformer/       # 查询转换器
│   │   ├── LoveAppDocumentLoader   # 文档加载
│   │   ├── MyKeywordEnricher       # 关键词增强
│   │   ├── MyQueryExpander         # 查询扩展
│   │   └── MyQueryRewriter         # 查询重写
│   ├── ratelimiter/                # 限流
│   │   ├── annotation/             # @RateLimit 注解
│   │   └── aspect/                 # 限流切面
│   └── tool/                       # 工具实现
│       ├── WebSearchTool           # 网络搜索
│       ├── WebScrapTool            # 网页爬取
│       ├── FileOperationTool       # 文件操作
│       ├── PDFGenerationTool       # PDF 生成
│       └── ...
├── src/main/resources/
│   ├── application.yml             # 主配置
│   ├── application-local.yml       # 本地配置
│   ├── prompt/                     # 提示词模板
│   └── document/love/              # 恋爱知识库
├── sql/                            # 数据库脚本
├── ai-agent-frontend/              # Vue 3 前端
└── image-search-mcp-server/        # 图片搜索 MCP 服务
```

## MCP 服务

项目内置了一个图片搜索 MCP 服务（`image-search-mcp-server`），基于 Pexels API 实现图片搜索功能，可通过 MCP 协议供智能体调用。

## 许可证

MIT License

## 作者

- 机hui难得（jiangtengjin）
- GitHub：https://github.com/jiangtengjin
