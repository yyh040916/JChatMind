package com.kama.myjchatmind.model.request;

public record CreateSessionRequest(
        String agentId,
        String title
) {
}
