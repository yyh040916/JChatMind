package com.kama.myjchatmind.model;

import java.time.LocalDateTime;

public class ChatMessage {

    private final String id;
    private final String sessionId;
    private final Role role;
    private final String content;
    private final LocalDateTime createdAt;

    public ChatMessage(String id, String sessionId, Role role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
