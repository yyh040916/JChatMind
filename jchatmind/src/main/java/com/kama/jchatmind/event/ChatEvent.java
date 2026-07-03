package com.kama.jchatmind.event;

import lombok.AllArgsConstructor;
import lombok.Data;
// 聊天事件类，包含聊天相关的信息，如代理ID、会话ID和用户输入
@Data
@AllArgsConstructor
public class ChatEvent {
    private String agentId;
    private String sessionId;
    private String userInput;
}
