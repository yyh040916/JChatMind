package com.kama.myjchatmind.repository;

import com.kama.myjchatmind.model.Agent;
import com.kama.myjchatmind.model.ChatMessage;
import com.kama.myjchatmind.model.ChatSession;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStore {

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ChatMessage> messages = new ConcurrentHashMap<>();

    public Agent saveAgent(Agent agent) {
        agents.put(agent.getId(), agent);
        return agent;
    }

    public List<Agent> listAgents() {
        return agents.values().stream()
                .sorted(Comparator.comparing(Agent::getCreatedAt))
                .toList();
    }

    public Optional<Agent> findAgent(String id) {
        return Optional.ofNullable(agents.get(id));
    }

    public ChatSession saveSession(ChatSession session) {
        sessions.put(session.getId(), session);
        return session;
    }

    public List<ChatSession> listSessions() {
        return sessions.values().stream()
                .sorted(Comparator.comparing(ChatSession::getCreatedAt))
                .toList();
    }

    public Optional<ChatSession> findSession(String id) {
        return Optional.ofNullable(sessions.get(id));
    }

    public ChatMessage saveMessage(ChatMessage message) {
        messages.put(message.getId(), message);
        return message;
    }

    public List<ChatMessage> listMessages(String sessionId) {
        return messages.values().stream()
                .filter(message -> message.getSessionId().equals(sessionId))
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .toList();
    }
}
