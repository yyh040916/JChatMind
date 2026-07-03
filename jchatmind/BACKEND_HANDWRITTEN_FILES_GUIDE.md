# JChatMind 后端手写文件学习指南

这份文档只讲 `D:\project\JChatMind\jchatmind` 后端目录。目标是帮你分清：

- 哪些文件是必须理解、必须手写的核心代码
- 哪些文件是业务支撑代码
- 哪些文件可以由 IDE、脚手架、MyBatis Generator、Lombok 等工具辅助生成
- 每个文件在项目里负责什么

先记住一句话：

```text
JChatMind 后端 = Spring Boot 接口层 + Agent 运行时 + Spring AI 模型调用 + 工具调用 + RAG 知识库 + PostgreSQL 持久化
```

## 0. 总体分层

后端主目录：

```text
jchatmind
  pom.xml
  src/main/java/com/kama/jchatmind
  src/main/resources
  jchatmind_v2/jchatmind_assert
```

核心包职责：

```text
config       Spring 配置、模型客户端配置、跨域、异步
controller   HTTP 接口入口
service      业务接口
service/impl 业务实现
agent        Agent 核心运行逻辑
agent/tools  模型可调用工具
event        用户消息触发 Agent 的事件机制
message      SSE 推送消息结构
mapper       MyBatis 数据库访问接口
resources/mapper MyBatis SQL XML
model        请求、响应、实体、DTO、VO
converter    对象转换
exception    统一异常处理
typehandler  PostgreSQL pgvector 类型转换
```

## 1. 项目启动与构建文件

### `pom.xml`

是否需要手写：半自动生成，后续必须手动维护。

作用：

- 声明这是一个 Maven 项目
- 指定 Spring Boot 版本
- 指定 Java 版本
- 引入 Spring AI BOM
- 引入 Web、JDBC、MyBatis、PostgreSQL、Spring AI DeepSeek、Spring AI ZhipuAI、Mail、Lombok 等依赖
- 配置 Spring Boot Maven 插件

学习重点：

```text
spring-ai-bom
spring-ai-starter-model-deepseek
spring-ai-starter-model-zhipuai
mybatis-spring-boot-starter
postgresql
```

这几个决定了项目为什么能：

```text
调用模型
连接数据库
使用 MyBatis
创建 DeepSeekChatModel / ZhiPuAiChatModel
```

### `mvnw` / `mvnw.cmd` / `.mvn/wrapper/maven-wrapper.properties`

是否需要手写：不需要，Maven Wrapper 自动生成。

作用：

- 让没有全局安装 Maven 的电脑也能运行 Maven 命令
- Windows 用 `mvnw.cmd`
- macOS/Linux 用 `mvnw`

平时不用研究，知道它是启动 Maven 的工具即可。

### `output.txt`

是否需要手写：不属于核心项目代码。

作用：

- 看起来像运行输出或临时日志
- 学习主线时可以忽略

## 2. 应用入口

### `src/main/java/com/kama/jchatmind/JchatmindApplication.java`

是否需要手写：通常由 Spring Initializr 自动生成，然后基本不动。

作用：

- Spring Boot 启动入口
- 包含 `main` 方法
- `@SpringBootApplication` 会触发组件扫描、自动配置、Bean 创建

你可以把它理解为：

```text
整个后端应用的总开关
```

## 3. config：配置层

### `config/MultiChatClientConfig.java`

是否需要手写：需要手写，AI 核心配置。

作用：

- 为每个模型创建一个 `ChatClient`
- 把 DeepSeek、智谱等不同厂商模型统一包装成 Spring AI 的 `ChatClient`

核心逻辑：

```java
@Bean("deepseek-chat")
public ChatClient deepSeekChatClient(DeepSeekChatModel model) {
    return ChatClient.create(model);
}
```

为什么重要：

```text
Agent 不直接依赖 DeepSeekChatModel 或 ZhiPuAiChatModel
Agent 只依赖统一的 ChatClient
```

如果以后加新模型，例如 `qwen-plus`，通常就在这里新增一个 `@Bean("qwen-plus")`。

### `config/ChatClientRegistry.java`

是否需要手写：需要手写，AI 核心配置。

作用：

- 收集 Spring 容器中所有 `ChatClient`
- 通过模型名获取对应客户端

核心思想：

```text
Map<String, ChatClient>
key   = Bean 名称，例如 deepseek-chat / glm-4.6
value = 对应 ChatClient
```

它连接了：

```text
Agent.model 字段
→ ChatClientRegistry.get(model)
→ 具体模型客户端
```

### `config/AsyncConfig.java`

是否需要手写：需要手写或按模板写。

作用：

- 开启异步能力
- 让 `@Async` 生效
- Agent 运行可以在后台线程执行，不阻塞用户发消息接口

和它相关的核心文件：

```text
event/listener/ChatEventListener.java
```

### `config/CorsConfig.java`

是否需要手写：常见模板代码。

作用：

- 允许前端跨域访问后端接口
- 开发环境里前端通常是 `localhost:5173`
- 后端通常是 `localhost:8080`
- 不配跨域，浏览器会拦截请求

它不是 AI 核心，但前后端联调必需。

## 4. controller：接口入口层

Controller 文件都需要手写，但大多是“薄薄一层”。它们不应该写复杂业务，而是把请求转交给 Service。

典型结构：

```text
Controller
→ Service
→ Mapper / Agent / RAG
```

### `controller/AgentController.java`

作用：

- 创建智能体
- 查询智能体列表
- 更新智能体
- 删除智能体

对应核心概念：

```text
Agent = 一个可配置的 AI 助手
```

Agent 配置通常包括：

```text
name
description
systemPrompt
model
allowedTools
allowedKbs
chatOptions
```

### `controller/ChatSessionController.java`

作用：

- 创建会话
- 查询会话
- 根据 Agent 查询会话
- 更新/删除会话

对应数据库表：

```text
chat_session
```

它解决的问题：

```text
一次连续聊天需要一个 sessionId
消息都挂在这个 sessionId 下面
```

### `controller/ChatMessageController.java`

作用：

- 保存用户消息
- 查询某个会话的消息
- 更新/删除消息

这是触发 AI 的关键入口之一。

用户发消息时大概链路是：

```text
ChatMessageController
→ ChatMessageFacadeServiceImpl.createChatMessage
→ 保存 user 消息
→ 发布 ChatEvent
→ 异步运行 Agent
```

### `controller/SseController.java`

作用：

- 建立 SSE 连接
- 前端通过 `EventSource` 连接这个接口
- 后端生成 AI 回复后，通过这条连接推给前端

它解决的问题：

```text
AI 回复不是瞬间完成的
需要后端主动把状态/结果推给前端
```

### `controller/KnowledgeBaseController.java`

作用：

- 创建知识库
- 查询知识库
- 修改知识库
- 删除知识库

知识库本身只是“容器”，真正内容来自文档和 chunk。

对应表：

```text
knowledge_base
```

### `controller/DocumentController.java`

作用：

- 上传文档
- 查询文档
- 删除文档
- 更新文档信息

它会触发文档处理流程：

```text
上传文件
→ 保存文件
→ 解析 Markdown
→ 切片
→ embedding
→ 存入 chunk_bge_m3
```

### `controller/ToolController.java`

作用：

- 查询可用工具列表
- 让前端创建 Agent 时能选择允许哪些工具

对应工具目录：

```text
agent/tools
```

### `controller/TestController.java`

作用：

- 测试接口
- 学习或调试用

一般不是正式业务核心。

## 5. service：业务接口层

`service` 包下的接口需要手写，但通常很短。它们定义“系统有什么能力”。

接口的作用：

```text
Controller 依赖接口
实现类写具体逻辑
方便替换、测试、分层
```

### `service/AgentFacadeService.java`

声明 Agent 相关能力：

- 创建 Agent
- 查询 Agent
- 更新 Agent
- 删除 Agent

实现类：

```text
service/impl/AgentFacadeServiceImpl.java
```

### `service/ChatSessionFacadeService.java`

声明会话相关能力：

- 创建会话
- 查询会话
- 根据 Agent 查询会话
- 更新/删除会话

实现类：

```text
service/impl/ChatSessionFacadeServiceImpl.java
```

### `service/ChatMessageFacadeService.java`

声明消息相关能力：

- 查询会话消息
- 查询最近 N 条消息
- 创建消息
- Agent 创建消息
- 更新/删除消息
- 追加消息内容

这是 AI 主链路里的关键接口。

实现类：

```text
service/impl/ChatMessageFacadeServiceImpl.java
```

### `service/SseService.java`

声明 SSE 推送能力：

- 建立连接
- 推送消息

实现类：

```text
service/impl/SseServiceImpl.java
```

### `service/ToolFacadeService.java`

声明工具管理能力：

- 查询所有工具
- 查询固定工具
- 查询可选工具

实现类：

```text
service/impl/ToolFacadeServiceImpl.java
```

### `service/RagService.java`

声明 RAG 能力：

- 文本 embedding
- 相似度搜索

实现类：

```text
service/impl/RagServiceImpl.java
```

### `service/KnowledgeBaseFacadeService.java`

声明知识库 CRUD 能力。

实现类：

```text
service/impl/KnowledgeBaseFacadeServiceImpl.java
```

### `service/DocumentFacadeService.java`

声明文档管理能力。

实现类：

```text
service/impl/DocumentFacadeServiceImpl.java
```

### `service/DocumentStorageService.java`

声明文件存储能力：

- 保存上传文件
- 删除文件
- 获取文件路径

实现类：

```text
service/impl/DocumentStorageServiceImpl.java
```

### `service/MarkdownParserService.java`

声明 Markdown 解析能力：

- 解析文档标题
- 解析文档结构

实现类：

```text
service/impl/MarkdownParserServiceImpl.java
```

### `service/EmailService.java`

声明邮件发送能力。

实现类：

```text
service/impl/EmailServiceImpl.java
```

## 6. service/impl：业务实现层

这些文件基本都需要手写，是项目真正业务逻辑所在地。

### `service/impl/ChatMessageFacadeServiceImpl.java`

AI 主链路关键文件。

核心职责：

- 查询会话消息
- 保存用户消息
- 保存 Agent 生成的消息
- 发布 `ChatEvent` 触发 Agent

最重要的方法：

```java
createChatMessage(CreateChatMessageRequest request)
```

它做了两件事：

```text
1. doCreateChatMessage(request) 保存 user 消息
2. publisher.publishEvent(new ChatEvent(...)) 触发 Agent
```

还要注意：

```java
agentCreateChatMessage(...)
```

这个方法给 Agent 保存消息用，不发布事件，避免无限循环：

```text
Agent 保存 assistant 消息
如果也发布 ChatEvent
就会再次触发 Agent
```

### `service/impl/AgentFacadeServiceImpl.java`

职责：

- 创建 Agent
- 把 request 转 DTO/entity
- 保存到 `agent` 表
- 查询、更新、删除 Agent

它是智能体配置的业务入口。

### `service/impl/ChatSessionFacadeServiceImpl.java`

职责：

- 创建聊天会话
- 查询会话
- 更新会话标题
- 删除会话

它本身不调用模型，但给消息和 Agent 提供上下文容器。

### `service/impl/SseServiceImpl.java`

AI 主链路关键文件。

职责：

- 用 `ConcurrentMap<String, SseEmitter>` 保存会话连接
- 前端连接时创建 `SseEmitter`
- 后端有 AI 消息时发送给前端

核心方法：

```java
connect(String chatSessionId)
send(String chatSessionId, SseMessage message)
```

这就是：

```text
后端主动推消息给前端
```

### `service/impl/ToolFacadeServiceImpl.java`

职责：

- 收集所有 Spring 容器中的工具 Bean
- 按工具类型区分固定工具、可选工具

它服务于：

```text
JChatMindFactory.resolveRuntimeTools(...)
```

### `service/impl/RagServiceImpl.java`

AI + RAG 核心文件。

职责：

- 调用 Ollama `/api/embeddings`
- 使用 `bge-m3` 生成向量
- 调用 `ChunkBgeM3Mapper` 查询相似 chunk

核心逻辑：

```text
query text
→ embedding
→ pgvector similarity search
→ related chunks
```

它是知识库问答的基础。

### `service/impl/DocumentFacadeServiceImpl.java`

职责：

- 上传文档
- 保存文件
- 创建 document 记录
- 解析 Markdown
- 把标题/片段生成 embedding
- 保存到 `chunk_bge_m3`

它把“普通文档”变成“可检索知识库向量”。

### `service/impl/DocumentStorageServiceImpl.java`

职责：

- 把上传的文件保存到本地磁盘
- 删除本地文件
- 通过相对路径找到文件

对应配置：

```yaml
document:
  storage:
    base-path: ./data/documents
```

### `service/impl/KnowledgeBaseFacadeServiceImpl.java`

职责：

- 知识库 CRUD
- 把 request/entity/vo 通过 converter 转换

### `service/impl/MarkdownParserServiceImpl.java`

职责：

- 解析 Markdown 文件
- 提取文档结构、标题、内容块

配合：

```text
DocumentFacadeServiceImpl
```

### `service/impl/EmailServiceImpl.java`

职责：

- 使用 Spring Mail 发送邮件
- 给 `EmailTools` 提供能力

### 其他 Impl 文件

CRUD 类实现都遵循类似模式：

```text
Request
→ Converter
→ Entity
→ Mapper
→ Response/VO
```

## 7. event：事件机制

### `event/ChatEvent.java`

是否需要手写：需要手写，AI 触发链路关键文件。

作用：

- 封装一次用户聊天事件
- 包含 `agentId`、`sessionId`、`userInput`

它是：

```text
用户消息保存完成
→ 通知后台 Agent 开始工作
```

### `event/listener/ChatEventListener.java`

是否需要手写：需要手写，AI 触发链路关键文件。

作用：

- 监听 `ChatEvent`
- 异步创建 `JChatMind`
- 调用 `jChatMind.run()`

核心逻辑：

```java
@Async
@EventListener
public void handle(ChatEvent event) {
    JChatMind jChatMind = jChatMindFactory.create(event.getAgentId(), event.getSessionId());
    jChatMind.run();
}
```

这说明：

```text
Controller 不直接调用模型
Service 保存消息后发布事件
监听器异步运行 Agent
```

## 8. agent：Agent 核心

这些文件是整个项目最值得手写和理解的部分。

### `agent/JChatMindFactory.java`

是否需要手写：必须手写，AI 核心。

作用：

- 根据 `agentId` 从数据库加载 Agent 配置
- 加载历史消息 memory
- 根据 Agent.model 选择 ChatClient
- 加载 Agent 允许使用的知识库
- 加载 Agent 允许使用的工具
- 把工具对象转换成 `ToolCallback`
- 创建真正运行的 `JChatMind`

你可以把它理解为：

```text
Agent 装配工厂
```

核心流程：

```text
create(agentId, chatSessionId)
→ loadAgent
→ toAgentConfig
→ loadMemory
→ resolveRuntimeKnowledgeBases
→ resolveRuntimeTools
→ buildToolCallbacks
→ buildAgentRuntime
```

最关键的一行：

```java
ChatClient chatClient = chatClientRegistry.get(agent.getModel());
```

它完成：

```text
Agent.model
→ 对应 ChatClient
→ 对应模型能力
```

### `agent/JChatMind.java`

是否需要手写：必须手写，AI 核心。

作用：

- 真正运行 Agent
- 调用模型
- 判断是否有工具调用
- 执行工具
- 保存 assistant/tool 消息
- 通过 SSE 推送结果

核心方法：

```text
run()
step()
think()
execute()
saveMessage(...)
```

主循环：

```text
run
→ step
→ think 调模型
→ 如果模型返回 tool_calls
→ execute 执行工具
→ 工具结果加入上下文
→ 再次 think
→ 最终 assistant 回复
```

这是 Agent 最核心的思想：

```text
模型负责判断
系统负责执行
结果再交给模型
```

### `agent/AgentState.java`

是否需要手写：需要手写或简单枚举。

作用：

- 描述 Agent 当前状态
- 例如空闲、运行中、结束等

它帮助 `JChatMind.run()` 控制执行流程。

### `agent/examples/JChatMindV1.java`

是否需要手写：示例代码，可选。

作用：

- 演示最基础 Agent
- 不一定是正式业务使用
- 适合学习从 0 到 1 的模型调用

可以先看它理解简单版，再看正式的 `JChatMind.java`。

### `agent/examples/JChatMindV2.java`

是否需要手写：示例代码，可选。

作用：

- 在 V1 基础上演示工具调用
- 展示 Spring AI `ToolCallingManager` 的用法

学习工具调用时很有价值。

## 9. agent/tools：模型可调用工具

工具类基本都需要手写，因为它们是你赋予模型的“外部能力”。

工具的共同特点：

```java
@org.springframework.ai.tool.annotation.Tool(...)
```

带这个注解的方法会被 Spring AI 转成模型可调用工具。

### `agent/tools/Tool.java`

作用：

- 项目自定义工具接口
- 让不同工具类具有统一类型

### `agent/tools/ToolType.java`

作用：

- 标记工具类型
- 常见是固定工具、可选工具

例如：

```text
FIXED    系统默认强制带上的工具
OPTIONAL Agent 配置中选择启用的工具
```

### `agent/tools/DirectAnswerTool.java`

作用：

- 允许模型直接回答
- 适合 Agent 认为不需要额外工具时结束任务

### `agent/tools/TerminateTool.java`

作用：

- 允许模型声明任务结束
- Agent 运行循环可以根据它判断是否停止

### `agent/tools/DataBaseTools.java`

作用：

- 给模型提供数据库查询能力
- 主要用于演示 AI 生成 SQL、执行 SQL、分析结果

安全重点：

```text
应该只允许 SELECT
不能允许 DELETE / UPDATE / INSERT / DROP
```

这个文件是工具调用学习重点。

### `agent/tools/KnowledgeTools.java`

作用：

- 给模型提供知识库查询能力
- 内部调用 `RagService.similaritySearch`

链路：

```text
模型调用 knowledgeQuery
→ KnowledgeTools
→ RagServiceImpl
→ pgvector 查相似 chunk
→ 返回知识片段
```

### `agent/tools/EmailTools.java`

作用：

- 给模型提供发送邮件能力
- 内部调用 `EmailService`

这是典型的“模型决策，系统执行”的工具。

### `agent/tools/FileSystemTools.java`

作用：

- 给模型提供文件系统操作能力
- 例如查看文件、写文件、目录操作等

学习时注意：

```text
文件系统工具权限风险很高
真实项目必须限制路径和操作范围
```

### `agent/tools/test/DateTool.java`

作用：

- 测试工具：获取日期

### `agent/tools/test/CityTool.java`

作用：

- 测试工具：获取城市

### `agent/tools/test/WeatherTool.java`

作用：

- 测试工具：获取天气

这三个适合学习最简单的 function calling。

## 10. message：SSE 消息结构

### `message/SseMessage.java`

是否需要手写：需要手写。

作用：

- 定义后端推给前端的消息格式
- 包含类型、payload、metadata

消息类型包括：

```text
AI_GENERATED_CONTENT  AI 生成内容
AI_PLANNING           AI 正在规划
AI_THINKING           AI 正在思考
AI_EXECUTING          AI 正在执行工具
AI_DONE               AI 完成
```

它连接：

```text
JChatMind
→ SseServiceImpl
→ 前端 EventSource
```

## 11. mapper：数据库访问接口

Mapper 接口需要写，但可以由 MyBatis Generator 辅助生成。

它们和 `src/main/resources/mapper/*.xml` 一一对应。

### `mapper/AgentMapper.java`

对应表：

```text
agent
```

作用：

- 插入 Agent
- 查询 Agent
- 更新 Agent
- 删除 Agent

### `mapper/ChatSessionMapper.java`

对应表：

```text
chat_session
```

作用：

- 管理会话记录

### `mapper/ChatMessageMapper.java`

对应表：

```text
chat_message
```

作用：

- 保存用户消息
- 保存 assistant 消息
- 保存 tool 消息
- 查询历史上下文
- 查询最近 N 条消息

这是 Agent memory 的数据来源。

### `mapper/KnowledgeBaseMapper.java`

对应表：

```text
knowledge_base
```

作用：

- 管理知识库

### `mapper/DocumentMapper.java`

对应表：

```text
document
```

作用：

- 管理上传文档记录

### `mapper/ChunkBgeM3Mapper.java`

对应表：

```text
chunk_bge_m3
```

作用：

- 保存文档切片
- 保存 embedding 向量
- 根据向量做相似度查询

这是 RAG 的核心 Mapper。

## 12. resources/mapper：MyBatis SQL XML

这些 XML 需要手写或由工具生成后手改。它们是真正 SQL 所在地。

### `resources/mapper/AgentMapper.xml`

作用：

- 写 `agent` 表相关 SQL
- 对应 `AgentMapper.java`

### `resources/mapper/ChatSessionMapper.xml`

作用：

- 写 `chat_session` 表相关 SQL

### `resources/mapper/ChatMessageMapper.xml`

作用：

- 写 `chat_message` 表相关 SQL
- 特别重要的是按 session 查询历史消息
- Agent 的 memory 来自这里

### `resources/mapper/KnowledgeBaseMapper.xml`

作用：

- 写 `knowledge_base` 表相关 SQL

### `resources/mapper/DocumentMapper.xml`

作用：

- 写 `document` 表相关 SQL

### `resources/mapper/ChunkBgeM3Mapper.xml`

作用：

- 写 `chunk_bge_m3` 表相关 SQL
- 包含 pgvector 查询

核心能力：

```sql
ORDER BY embedding <-> #{vectorLiteral}::vector
```

类似这样的 SQL 用来做向量相似度排序。

## 13. typehandler：特殊数据库类型转换

### `typehandler/PgVectorTypeHandler.java`

是否需要手写：需要手写。

作用：

- 让 Java 的 `float[]` 和 PostgreSQL pgvector 类型互相转换

为什么需要：

```text
PostgreSQL vector 字段不是普通字符串/数字
MyBatis 不知道怎么自动映射
所以需要 TypeHandler
```

它服务于：

```text
ChunkBgeM3.embedding
```

## 14. converter：对象转换层

Converter 文件通常需要手写，但可以用 MapStruct 等工具生成。本项目是手写。

为什么需要 converter：

```text
Controller 接收 Request
Service 内部使用 DTO
Mapper 保存 Entity
前端展示 VO
```

同一个业务对象在不同层有不同形态，所以需要转换。

### `converter/AgentConverter.java`

作用：

- `CreateAgentRequest` → `AgentDTO`
- `AgentDTO` → `Agent`
- `Agent` → `AgentVO`
- 处理 `model`、`allowedTools`、`allowedKbs`、`chatOptions`

### `converter/ChatMessageConverter.java`

作用：

- 消息 request/entity/dto/vo 转换
- 处理消息 metadata
- metadata 里可能包含工具调用、RAG 片段、工具响应等信息

### `converter/ChatSessionConverter.java`

作用：

- 会话 request/entity/dto/vo 转换

### `converter/KnowledgeBaseConverter.java`

作用：

- 知识库 request/entity/dto/vo 转换

### `converter/DocumentConverter.java`

作用：

- 文档 request/entity/dto/vo 转换

### `converter/ChunkBgeM3Converter.java`

作用：

- 文档 chunk 的 entity/dto 转换
- 包含 embedding 字段

## 15. model：数据结构层

这些文件数量很多，但大部分结构相似。可以手写，也可以由工具生成。

### 15.1 `model/entity`

Entity 对应数据库表。

需要理解：

```text
Entity = 数据库表在 Java 里的表示
```

文件：

```text
Agent.java
ChatSession.java
ChatMessage.java
KnowledgeBase.java
Document.java
ChunkBgeM3.java
```

作用分别是：

```text
Agent          agent 表，智能体配置
ChatSession    chat_session 表，会话
ChatMessage    chat_message 表，消息
KnowledgeBase  knowledge_base 表，知识库
Document       document 表，文档
ChunkBgeM3     chunk_bge_m3 表，文档切片和向量
```

### 15.2 `model/request`

Request 是前端传给后端的数据。

文件：

```text
CreateAgentRequest.java
UpdateAgentRequest.java
CreateChatSessionRequest.java
UpdateChatSessionRequest.java
CreateChatMessageRequest.java
UpdateChatMessageRequest.java
CreateKnowledgeBaseRequest.java
UpdateKnowledgeBaseRequest.java
CreateDocumentRequest.java
UpdateDocumentRequest.java
```

作用：

```text
定义接口入参
```

例如创建 Agent 时，前端需要传：

```text
name
description
systemPrompt
model
allowedTools
allowedKbs
chatOptions
```

### 15.3 `model/response`

Response 是接口返回结构。

文件：

```text
CreateAgentResponse.java
GetAgentsResponse.java
CreateChatSessionResponse.java
GetChatSessionResponse.java
GetChatSessionsResponse.java
CreateChatMessageResponse.java
GetChatMessagesResponse.java
CreateKnowledgeBaseResponse.java
GetKnowledgeBasesResponse.java
CreateDocumentResponse.java
GetDocumentsResponse.java
```

作用：

```text
定义接口返回给前端的数据外壳
```

### 15.4 `model/vo`

VO 是前端展示用对象。

文件：

```text
AgentVO.java
ChatSessionVO.java
ChatMessageVO.java
KnowledgeBaseVO.java
DocumentVO.java
```

作用：

```text
屏蔽数据库字段
只返回前端需要展示的数据
```

### 15.5 `model/dto`

DTO 是业务内部传递对象。

文件：

```text
AgentDTO.java
ChatSessionDTO.java
ChatMessageDTO.java
KnowledgeBaseDTO.java
DocumentDTO.java
ChunkBgeM3DTO.java
```

作用：

```text
Service 层内部使用
避免 Controller 直接操作数据库 Entity
```

### 15.6 `model/common/ApiResponse.java`

作用：

- 统一接口返回格式
- 通常包含 code、message、data

这样前端可以用统一方式处理成功/失败。

## 16. exception：异常处理

### `exception/BizException.java`

是否需要手写：需要手写。

作用：

- 表示业务异常
- 例如“知识库不存在”“创建消息失败”

### `exception/GlobalExceptionHandler.java`

是否需要手写：需要手写或模板化。

作用：

- 统一捕获异常
- 返回统一的 `ApiResponse`
- 避免后端异常栈直接暴露给前端

## 17. resources/application.yaml

是否需要手写：需要配置，不能完全自动生成。

作用：

- 配置应用名
- 配置 PostgreSQL 数据库
- 配置邮件服务
- 配置 DeepSeek API Key / base-url / model
- 配置智谱 API Key / base-url / model
- 配置 MyBatis
- 配置文档存储路径

重要提醒：

```text
真实 API Key、邮箱授权码不要提交到 GitHub
```

学习阶段可以本地写死，正式项目建议改成环境变量：

```yaml
api-key: ${DEEPSEEK_API_KEY}
```

## 18. jchatmind_v2/jchatmind_assert：数据库脚本

这些 SQL 文件需要手写或由数据库建模工具生成后手改。

### `jchatmind.sql`

作用：

- 创建 JChatMind 主业务表
- 创建 pgvector 扩展
- 创建 Agent、会话、消息、知识库、文档、chunk 等核心表

核心表：

```text
agent
chat_session
chat_message
knowledge_base
document
chunk_bge_m3
```

### `eshop.sql`

作用：

- 创建电商演示业务表
- 给 AI SQL 查询工具提供示例数据库结构

### `eshop_data.sql`

作用：

- 插入电商演示数据
- 必须在 `eshop.sql` 后执行

### `eshop.md`

作用：

- 说明电商示例数据库结构
- 帮助理解 AI 生成 SQL 时能查询哪些表

## 19. test：测试文件

测试文件需要手写，但可以先不看。

文件：

```text
src/test/java/com/kama/jchatmind/JChatMindTests.java
src/test/java/com/kama/jchatmind/JchatmindApplicationTests.java
src/test/java/com/kama/jchatmind/agent/examples/JChatMindV1Test.java
src/test/java/com/kama/jchatmind/agent/examples/JChatMindV2Test.java
```

作用：

- 测试 Spring Boot 是否能启动
- 测试 Agent 示例
- 测试工具调用流程

## 20. 哪些是真正必须优先手写和理解的文件

如果你的目标是学习后端 AI，不要平均用力。优先看这些：

```text
config/MultiChatClientConfig.java
config/ChatClientRegistry.java

service/impl/ChatMessageFacadeServiceImpl.java
event/ChatEvent.java
event/listener/ChatEventListener.java

agent/JChatMindFactory.java
agent/JChatMind.java

agent/tools/DirectAnswerTool.java
agent/tools/TerminateTool.java
agent/tools/DataBaseTools.java
agent/tools/KnowledgeTools.java

service/impl/RagServiceImpl.java
message/SseMessage.java
service/impl/SseServiceImpl.java
```

这些文件构成 AI 主链路：

```text
用户发消息
→ 保存消息
→ 发布事件
→ 创建 Agent
→ 选择模型
→ 加载工具
→ 调用模型
→ 执行工具
→ 保存 AI 回复
→ SSE 推给前端
```

## 21. 哪些文件可以暂时只知道作用

这些先不用逐行看：

```text
model/request/*
model/response/*
model/vo/*
model/dto/*
converter/*
mapper/*
resources/mapper/*
controller 的大部分 CRUD
DocumentStorageServiceImpl
EmailServiceImpl
MarkdownParserServiceImpl
```

原因：

```text
它们是工程支撑层
不是 AI 思维链路本身
```

但后面做完整项目时，这些又都需要补回来。

## 22. 手写、自动生成、半自动生成分类

### 必须手写

```text
agent/JChatMind.java
agent/JChatMindFactory.java
agent/tools/*.java
config/MultiChatClientConfig.java
config/ChatClientRegistry.java
event/*.java
message/SseMessage.java
service/impl/*.java
controller/*.java
exception/*.java
typehandler/PgVectorTypeHandler.java
```

### 可以手写，也可以工具辅助生成

```text
model/entity/*.java
model/dto/*.java
model/vo/*.java
model/request/*.java
model/response/*.java
converter/*.java
mapper/*.java
resources/mapper/*.xml
SQL 脚本
```

### 通常自动生成

```text
mvnw
mvnw.cmd
.mvn/wrapper/*
target/*
基础启动类 JchatmindApplication.java
部分测试骨架
```

### 手写但模板化程度高

```text
CorsConfig.java
AsyncConfig.java
GlobalExceptionHandler.java
ApiResponse.java
Controller CRUD 方法
Service 接口
```

## 23. 推荐阅读顺序

第一轮只看主链路：

```text
1. MultiChatClientConfig
2. ChatClientRegistry
3. ChatMessageFacadeServiceImpl.createChatMessage
4. ChatEvent
5. ChatEventListener
6. JChatMindFactory.create
7. JChatMind.run / step / think / execute
8. SseMessage
9. SseServiceImpl
```

第二轮看工具：

```text
1. Tool.java
2. ToolType.java
3. DirectAnswerTool.java
4. TerminateTool.java
5. DataBaseTools.java
6. KnowledgeTools.java
7. ToolFacadeServiceImpl.java
8. JChatMindFactory.buildToolCallbacks
```

第三轮看 RAG：

```text
1. jchatmind.sql 里的 knowledge_base / document / chunk_bge_m3
2. DocumentFacadeServiceImpl
3. MarkdownParserServiceImpl
4. RagServiceImpl
5. ChunkBgeM3Mapper.java
6. ChunkBgeM3Mapper.xml
7. PgVectorTypeHandler.java
```

第四轮看工程支撑：

```text
controller
service interface
converter
mapper
model
exception
```

## 24. 你应该能回答的问题

学完这份文件后，尝试回答：

```text
1. 用户发消息后，是哪个方法保存到 chat_message 表的？
2. 为什么 createChatMessage 会发布 ChatEvent？
3. ChatEventListener 为什么要 @Async？
4. JChatMindFactory 为什么要根据 agent.getModel() 找 ChatClient？
5. ToolCallback 是怎么从工具类生成出来的？
6. 模型什么时候会返回 tool_calls？
7. 工具执行结果怎么重新放回上下文？
8. assistant 回复是怎么保存的？
9. SSE 是怎么把 AI 结果推给前端的？
10. bge-m3 和 chunk_bge_m3 表有什么关系？
```

能讲清这 10 个问题，JChatMind 后端 AI 主体就基本吃透了。

