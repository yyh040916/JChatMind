package com.kama.myjchatmind.model;

import java.time.LocalDateTime;

public class ChatSession {

    private final String id;
    private final String agentId;
    private final String title;
    private final LocalDateTime createdAt;

    public ChatSession(String id, String agentId, String title, LocalDateTime createdAt) {
        this.id = id;
        this.agentId = agentId;
        this.title = title;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
