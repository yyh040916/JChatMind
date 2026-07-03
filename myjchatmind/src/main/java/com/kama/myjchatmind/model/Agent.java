package com.kama.myjchatmind.model;

import java.time.LocalDateTime;

public class Agent {

    private final String id;
    private final String name;
    private final String systemPrompt;
    private final String model;
    private final LocalDateTime createdAt;

    public Agent(String id, String name, String systemPrompt, String model, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.systemPrompt = systemPrompt;
        this.model = model;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public String getModel() {
        return model;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
