# AI Agent - 智能体助手平台

## 项目定位
基于 Spring AI 的全功能 AI 智能体平台，集多模型接入、RAG 知识库、工具调用、会话管理于一体。内置两大应用：
- **AI 恋爱大师**：RAG 驱动的恋爱咨询助手（按单身/恋爱/已婚状态提供建议）
- **AI 超级智能体**：ReAct 模式的通用智能体（自主思考、工具调用、多步骤执行）

## 技术栈

**后端：** Spring Boot 3.5.7 + JDK 21 + Spring AI 1.0.0-M6 | MyBatis-Plus 3.5.14 + MySQL 8.0 | Redis + Redisson 3.52.0 | PGVector 向量库 | Knife4j 4.4.0 | iText 9.1.0 | Jsoup 1.19.1 | Hutool 5.8.37

**前端：** Vue 3 + TypeScript + Vite 5 + Ant Design Vue + Axios + Vue Router 4

**MCP 服务：** image-search-mcp-server（Pexels 图片搜索，端口 8127，支持 SSE/Stdio 双模式）

## 项目结构（三级）

```
ai-agent/
├── src/main/java/com/xhh/aiagent/
│   ├── advisor/                     # 可插拔 Advisor 增强
│   │   ├── CustomLoggerAdvisor       # 自定义日志
│   │   ├── ReReadAdvisor             # R2 重复读取推理增强
│   │   └── UserMessageCheckAdvisor   # 敏感词 + Prompt 注入检测
│   ├── aiinterruptor/               # 会话中断管理
│   │   └── SessionManager
│   ├── app/                         # 应用层（恋爱大师主逻辑）
│   │   └── LoveApp
│   ├── chatmemory/                  # 对话记忆持久化
│   │   ├── FileBasedChatMemory      # 文件（Kryo 序列化）
│   │   └── InDBChatMemory           # 数据库
│   ├── common/                      # 通用响应/请求对象
│   │   ├── BaseResponse / ResultUtils / DeleteRequest / PageRequest
│   ├── config/                      # Spring 配置
│   │   ├── CorsConfig / JsonConfig / WebMvcConfig
│   ├── constant/                    # 常量
│   │   ├── FileConstant / UserConstant / WebSearchConstant
│   ├── controller/                  # REST 控制器
│   │   ├── AiController             # AI 对话（恋爱大师 + 超级智能体，SSE 流式）
│   │   ├── CaptchaController        # 图片验证码
│   │   ├── ChatHistoryController     # 对话历史
│   │   ├── CodeMailController       # 邮箱验证码
│   │   ├── ConversationController   # 会话 CRUD + 游标分页
│   │   └── UserController           # 注册/登录/查询
│   ├── converter/                   # 消息转换器
│   ├── demo/invoke/                 # AI 调用示例（6 种方式）
│   ├── exception/                   # 全局异常处理
│   │   ├── BusinessException / ErrorCode / GlobalExceptionHandler / ManusStateException
│   ├── interceptor/                 # 登录拦截器
│   │   └── LoginInterceptor
│   ├── manager/                     # 验证码 & 邮箱管理
│   │   ├── captcha/CaptchaManager
│   │   └── codemail/CodeMailManager
│   ├── manus/                       # ★ 自研智能体引擎（核心）
│   │   ├── BaseAgent                # 基类（状态机 + 内存 + 执行循环）
│   │   ├── ReActAgent               # ReAct 模式（Think-Act 循环）
│   │   ├── ToolCallAgent            # 工具调用处理
│   │   └── MyManus                  # 具体智能体实现
│   ├── mapper/                      # MyBatis-Plus Mapper
│   │   ├── UserMapper / ConversationMapper / ChatHistoryMapper
│   ├── model/                       # 数据模型
│   │   ├── entity/                  # 数据库实体（User / Conversation / ChatHistory）
│   │   ├── enums/                   # AgentMessageType / AgentState / DaskStatus
│   │   ├── request/                 # 请求 DTO
│   │   └── vo/                      # 视图对象
│   ├── rag/                         # ★ RAG 完整链路
│   │   ├── documentreader/          # 文档读取（GitHub）
│   │   ├── querytransformer/        # 查询转换
│   │   ├── LoveAppDocumentLoader    # 文档加载
│   │   ├── MyKeywordEnricher        # 关键词增强
│   │   ├── MyQueryExpander          # 查询扩展
│   │   ├── MyQueryRewriter          # 查询重写
│   │   └── *Config                  # 向量存储 / 知识库配置
│   ├── ratelimiter/                 # 分布式限流（Redisson）
│   │   ├── annotation/@RateLimit
│   │   ├── aspect/RateLimitAspect
│   │   └── enums/RateLimitType       # 用户级 / 接口级 / IP 级
│   ├── service/                     # 业务服务层
│   │   ├── impl/                    # 实现类
│   │   ├── UserService / ConversationService / ChatHistoryService
│   └── tool/                        # ★ 工具系统（9 种）
│       ├── WebSearchTool            # 网络搜索（SerpAPI）
│       ├── WebScrapTool             # 网页爬取（Jsoup）
│       ├── FileOperationTool        # 文件读写
│       ├── PDFGenerationTool        # PDF 生成（iText）
│       ├── ResourceDownloadTool     # 资源下载
│       ├── TerminalOperationTool    # 终端命令执行
│       ├── QQEmailSenderTool        # QQ 邮件发送
│       ├── AskHumanTool             # 向人类求助
│       ├── TerminateTool            # 任务终止
│       └── ToolRegister             # 工具统一注册管理
│
├── src/main/resources/
│   ├── application.yml              # 主配置（MySQL / Redis / Session / 端口 8123）
│   ├── application-local.yml        # 本地开发配置（API Key / MCP / 邮箱等）
│   ├── mcp-servers.json             # MCP 服务注册（高德地图 + 图片搜索）
│   ├── prompt/                      # 系统提示词
│   │   └── love-assistant-system-prompt.txt
│   ├── document/love/               # RAG 恋爱知识库（4 篇 Markdown）
│   └── mapper/                      # MyBatis-Plus XML
│
├── ai-agent-frontend/               # Vue 3 前端
│   └── src/
│       ├── api/                     # Axios 封装 + 接口定义
│       │   ├── index.ts             # Axios 实例（baseURL: /api, timeout: 180s）
│       │   ├── ai.ts / conversation.ts / types.ts
│       ├── router/index.ts          # 路由配置
│       ├── utils/sse.ts             # SSE 工具类
│       └── views/                   # 页面
│           ├── Home.vue             # 主页（应用选择）
│           ├── LoveMaster.vue       # AI 恋爱大师（流式对话）
│           └── SuperAgent.vue       # AI 超级智能体（思考过程展示）
│
├── image-search-mcp-server/         # MCP 子服务（Pexels 图片搜索）
│   └── src/main/java/com/xhh/imagesearchmcpserver/
│       └── tool/ImageSearchTool     # 图片搜索工具
│
└── sql/                             # 数据库初始化
    └── create_table.sql             # user / chat_history / conversation
```

## 分层架构规范

```
Controller（接收请求）
    → Service（业务逻辑）
        → Mapper（数据库操作）
    → Manus（智能体引擎）
        → Tool（工具调用）
    → RAG（知识检索）
```

### 分层职责
| 层次 | 路径 | 职责 |
|------|------|------|
| Controller | `controller/` | 接收 HTTP 请求，参数校验，调用 Service/Manus/App，返回结果。方法名 `doXxx` |
| Service | `service/` + `impl/` | 业务逻辑编排，接口 + 实现分离。命名 `XxxService` / `XxxServiceImpl` |
| Manus | `manus/` | 智能体执行引擎。继承 `BaseAgent → ReActAgent → ToolCallAgent → MyManus` |
| Tool | `tool/` | 工具实现。实现 `ToolCallback` 接口，通过 `ToolRegister` 注册 |
| RAG | `rag/` | 知识检索链路。文档加载 → 切分 → 增强 → 向量化 → 检索 |
| Mapper | `mapper/` | MyBatis-Plus 映射器。继承 `BaseMapper<Xxx>` |
| Entity | `model/entity/` | 数据库实体。表名 → 类名（下划线转驼峰） |
| Advisor | `advisor/` | 实现 `AroundAdvisor` 接口，通过 `AdvisorOrder` 排序 |

## 编码规范

### 命名规范
| 类别 | 规则 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `UserService`, `BaseAgent` |
| 方法名 | 小驼峰 | `doChat()`, `getUserById()` |
| 常量 | 全大写下划线 | `UserConstant.USER_LOGIN_STATE` |
| 包名 | 全小写 | `com.xhh.aiagent.manus` |
| 枚举 | 大驼峰类 + 全大写下划线值 | `AgentState.IDLE`, `RateLimitType.USER` |
| Mapper XML | 类名小写 `.xml` | `UserMapper.xml` |
| 配置文件 | kebab-case | `application-local.yml` |

### 代码约定
1. **依赖注入**：使用 `@Resource` 注解（jakarta），不使用 `@Autowired`
2. **请求映射**：Controller 使用 `@GetMapping` / `@PostMapping` 等具体注解，路径小写 + 中划线
3. **统一响应**：所有接口返回 `BaseResponse<T>`，通过 `ResultUtils.success()` / `ResultUtils.error()` 构造
4. **异常处理**：业务异常抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一捕获
5. **参数校验**：基础类型在 Controller 层手动校验，复杂对象使用 `@Validated`
6. **日志**：使用 `@Slf4j` + Lombok，不使用 `System.out`
7. **Lombok**：常用 `@Data`, `@Slf4j`, `@Resource`, `@Getter`, `@Builder`
8. **实体设计**：所有表包含 `id`, `createTime`, `updateTime`, `isDelete`（逻辑删除：0-未删，1-已删）
9. **分页查询**：使用游标分页（cursor-based），不使用 OFFSET 分页
10. **SSE 通信**：AI 对话使用 SSE 流式输出，前端用 `EventSource` / 自定义 SSE 工具
11. **限流**：接口级别使用 `@RateLimit(type = RateLimitType.XXX, count = N, time = T)` 注解
12. **配置激活**：通过 `spring.profiles.active: local` 激活 `application-local.yml`

### 数据库规范
- **数据库名**：`ai_agent`
- **表命名**：小写 + 下划线（`chat_history`, `conversation`）
- **字段命名**：小驼峰（`userAccount`, `conversationId`）
- `mybatis-plus.configuration.map-underscore-to-camel-case: false`
- 逻辑删除字段 `isDelete`，类型 `tinyint`

## 关键业务流程

### AI 恋爱大师流程
```
用户输入 → LoveApp.doChat()
    → 构建 ChatClient.PromptSpec（含系统提示词 + 对话历史 + RAG 上下文）
    → Advisor 链执行（日志 → R2 推理 → 安全检测）
    → ChatModel 调用（DashScope/Ollama）
    → 流式 SSE 返回给前端
```

### AI 超级智能体流程
```
用户输入 → MyManus.run()
    → BaseAgent 状态机（IDLE → RUNNING → FINISHED/ERROR）
    → ReActAgent 循环（最多 maxSteps=20 步）：
        1. Think：LLM 分析当前状态，决定下一步行动
        2. Act：执行选定的 Tool（或发出 Final Answer）
        3. Observe：收集工具执行结果
        4. 更新内存 → 进入下一轮 Think
    → 每一步通过 SSE 推送给前端
```

### RAG 检索链路
```
文档(Markdown) → GitHubDocumentReader 读取
    → MyTokenTextSplitter 切分（按 token）
    → MyKeywordEnricher 关键词增强
    → 向量化 → 存入 VectorStore（SimpleVectorStore / PGVector / DashScope）

用户查询 → MyQueryRewriter 重写
    → MyQueryExpander 扩展
    → MyTranslationQueryTransformer 翻译（可选）
    → 向量检索 → 返回相关文档段落
```

## 配置说明

| 配置文件 | 用途 | 关键项 |
|----------|------|--------|
| `application.yml` | 主配置，通用设置 | port=8123, context-path=/api, MySQL, Redis, MyBatis-Plus, Session 30天 |
| `application-local.yml` | 本地开发，敏感配置 | DashScope/Ollama API Key, MCP server, QQ邮箱, SerpAPI |
| `mcp-servers.json` | MCP 服务注册 | 高德地图 MCP + image-search-mcp-server（Stdio） |
| 前端 `.env` / `vite.config.ts` | 代理 `/api` → `localhost:8123` | 开发端口 5173 |

## 开发指南

### 启动顺序
1. **数据库**：`mysql -u root -p < sql/create_table.sql`
2. **后端**：配置 `application-local.yml` 中的 API Key → `./mvnw spring-boot:run`
3. **前端**：`cd ai-agent-frontend && npm install && npm run dev`
4. **MCP 服务**（可选）：`cd image-search-mcp-server && ./mvnw spring-boot:run`

### 常用命令
```bash
./mvnw spring-boot:run                  # 启动后端（端口 8123）
./mvnw test                             # 运行测试
./mvnw clean package -DskipTests        # 打包
```

### 接口文档
启动后端后访问：`http://localhost:8123/api/doc.html`（Knife4j）

### 测试目录
`src/test/java/com/xhh/aiagent/` — 包含对工具、RAG、Manus、应用的 17 个测试类

## 外部依赖

| 服务 | 用途 | 必需？ |
|------|------|--------|
| 阿里云 DashScope | 大模型 API | 是 |
| MySQL 8.0+ | 关系数据库 | 是 |
| Redis 6.0+ | 缓存 / Session / 限流 | 是 |
| Ollama（可选） | 本地模型 | 否 |
| PGVector（可选） | 向量数据库 | 否 |
| SerpAPI | 网络搜索 | 否（影响 WebSearchTool） |
| Pexels API | 图片搜索 | 否（影响 MCP 服务） |
| 高德地图 MCP | 地图服务 | 否 |

## 关键路径速查

| 功能点 | 入口文件 | 核心逻辑 |
|--------|----------|----------|
| 系统启动 | `AiAgentApplication.java` | Spring Boot 入口 |
| 恋爱大师对话 | `AiController.java:49-80` | SSE 流式 → `LoveApp` |
| 超级智能体 | `AiController.java:81-145` | SSE 流式 → `MyManus` |
| 智能体状态机 | `BaseAgent.java` | IDLE→RUNNING→FINISHED/ERROR |
| ReAct 循环 | `ReActAgent.java` | Think→Act→Observe 循环 |
| 工具调用 | `ToolCallAgent.java` | LLM 选择工具 + 参数 → 执行 |
| 工具注册 | `ToolRegister.java` | 集中注册所有 ToolCallback |
| 对话记忆 | `InDBChatMemory.java` | 数据库持久化对话历史 |
| RAG 文档加载 | `LoveAppDocumentLoader.java` | 读取 → 切分 → 向量化 |
| 关键词增强 | `MyKeywordEnricher.java` | 提取实体补充检索 |
| 查询重写 | `MyQueryRewriter.java` | 多视角重写用户问题 |
| 限流注解 | `@RateLimit` | Redisson RRateLimiter |
| 安全检测 | `UserMessageCheckAdvisor.java` | 敏感词 + Prompt 注入 |
| 前端 SSE | `utils/sse.ts` | EventSource 封装 |
| 登录拦截 | `LoginInterceptor.java` | Session 校验 |
