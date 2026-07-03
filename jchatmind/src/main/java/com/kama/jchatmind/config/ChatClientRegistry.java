package com.kama.jchatmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

// 聊天客户端注册表，用于存储和管理不同聊天模型的ChatClient实例
// 通过构造函数注入一个Map<String, ChatClient>，其中key是聊天模型的标识（如"deepseek-chat"或"glm-4.6"），value是对应的ChatClient实例
// 提供一个get方法，根据key获取对应的ChatClient实例，方便在应用中根据需要选择不同的聊天模型进行交互
@Component
public class ChatClientRegistry {

    private final Map<String, ChatClient> chatClients;//final修饰符表示chatClients变量在初始化后不能被重新赋值，确保了注册表中的ChatClient实例的稳定性和一致性

    public ChatClientRegistry(Map<String, ChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    public ChatClient get(String key) {
        return chatClients.get(key);
    }
}
