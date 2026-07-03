package com.kama.myjchatmind.service.impl;

import com.kama.myjchatmind.agent.SimpleAgent;
import com.kama.myjchatmind.agent.SimpleAgentFactory;
import com.kama.myjchatmind.model.ChatMessage;
import com.kama.myjchatmind.model.Role;
import com.kama.myjchatmind.model.request.SendMessageRequest;
import com.kama.myjchatmind.model.response.SendMessageResponse;
import com.kama.myjchatmind.repository.InMemoryStore;
import com.kama.myjchatmind.service.ChatMessageService;
import com.kama.myjchatmind.service.ChatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final InMemoryStore store;
    private final ChatSessionService chatSessionService;
    private final SimpleAgentFactory simpleAgentFactory;

    public ChatMessageServiceImpl(
            InMemoryStore store,
            ChatSessionService chatSessionService,
            SimpleAgentFactory simpleAgentFactory
    ) {
        this.store = store;
        this.chatSessionService = chatSessionService;
        this.simpleAgentFactory = simpleAgentFactory;
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) {
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("Message content is required");
        }

        var session = chatSessionService.getSession(request.sessionId());
        ChatMessage userMessage = store.saveMessage(new ChatMessage(
                UUID.randomUUID().toString(),
                session.getId(),
                Role.USER,
                request.content(),
                LocalDateTime.now()
        ));

        SimpleAgent agent = simpleAgentFactory.create(session.getAgentId(), session.getId());
        ChatMessage assistantMessage = agent.reply();
        return new SendMessageResponse(userMessage, assistantMessage, store.listMessages(session.getId()));
    }

    @Override
    public List<ChatMessage> listMessages(String sessionId) {
        chatSessionService.getSession(sessionId);
        return store.listMessages(sessionId);
    }
}
