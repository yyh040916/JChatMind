package com.kama.myjchatmind.service.impl;

import com.kama.myjchatmind.model.ChatSession;
import com.kama.myjchatmind.model.request.CreateSessionRequest;
import com.kama.myjchatmind.repository.InMemoryStore;
import com.kama.myjchatmind.service.AgentService;
import com.kama.myjchatmind.service.ChatSessionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    private final InMemoryStore store;
    private final AgentService agentService;

    public ChatSessionServiceImpl(InMemoryStore store, AgentService agentService) {
        this.store = store;
        this.agentService = agentService;
    }

    @Override
    public ChatSession createSession(CreateSessionRequest request) {
        agentService.getAgent(request.agentId());
        String title = StringUtils.hasText(request.title()) ? request.title() : "新的对话";

        return store.saveSession(new ChatSession(
                UUID.randomUUID().toString(),
                request.agentId(),
                title,
                LocalDateTime.now()
        ));
    }

    @Override
    public List<ChatSession> listSessions() {
        return store.listSessions();
    }

    @Override
    public ChatSession getSession(String sessionId) {
        return store.findSession(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
    }
}
