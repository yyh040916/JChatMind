package com.kama.myjchatmind.model.request;

public record SendMessageRequest(
        String sessionId,
        String content
) {
}
