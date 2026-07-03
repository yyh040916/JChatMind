package com.kama.myjchatmind.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ChatClientRegistry {

    private final Map<String, ChatClient> chatClients;

    public ChatClientRegistry(Map<String, ChatClient> chatClients) {
        this.chatClients = chatClients;
    }

    public ChatClient get(String model) {
        return chatClients.get(model);
    }
}
