package com.kama.myjchatmind.model.request;

public record CreateAgentRequest(
        String name,
        String systemPrompt,
        String model
) {
}
