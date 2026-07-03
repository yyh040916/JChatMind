package com.kama.myjchatmind.agent;

import com.kama.myjchatmind.model.Agent;
import com.kama.myjchatmind.model.ChatMessage;
import com.kama.myjchatmind.model.Role;
import com.kama.myjchatmind.repository.InMemoryStore;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleAgent {

    private final Agent agent;
    private final String sessionId;
    private final ChatClient chatClient;
    private final InMemoryStore store;

    public SimpleAgent(Agent agent, String sessionId, ChatClient chatClient, InMemoryStore store) {
        this.agent = agent;
        this.sessionId = sessionId;
        this.chatClient = chatClient;
        this.store = store;
    }

    public ChatMessage reply() {
        List<Message> messages = buildPromptMessages();
        String content = chatClient
                .prompt()
                .messages(messages)
                .call()
                .content();

        return store.saveMessage(new ChatMessage(
                UUID.randomUUID().toString(),
                sessionId,
                Role.ASSISTANT,
                content,
                LocalDateTime.now()
        ));
    }

    private List<Message> buildPromptMessages() {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(agent.getSystemPrompt()));

        for (ChatMessage message : store.listMessages(sessionId)) {
            if (message.getRole() == Role.USER) {
                messages.add(new UserMessage(message.getContent()));
            } else if (message.getRole() == Role.ASSISTANT) {
                messages.add(new AssistantMessage(message.getContent()));
            }
        }

        return messages;
    }
}
