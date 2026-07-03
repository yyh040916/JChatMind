# MyJChatMind

一个最小版 JChatMind，用来学习后端 AI 调用链。

## 功能

- 创建智能体
- 创建会话
- 发送用户消息
- 调用 DeepSeek
- 保存用户和助手消息到内存
- 用一个简略 HTML 页面测试

## 启动

先设置 DeepSeek API Key：

```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek API Key"
```

启动后端：

```powershell
cd D:\project\JChatMind\myjchatmind
.\mvnw.cmd spring-boot:run
```

也可以用 IDE 直接运行 `MyJChatMindApplication`。

访问页面：

```text
http://localhost:8090
```

## 后端主线

```text
ChatMessageController
-> ChatMessageServiceImpl
-> SimpleAgentFactory
-> SimpleAgent
-> ChatClient
-> DeepSeek
```
