package com.kama.myjchatmind.model.response;

import com.kama.myjchatmind.model.ChatMessage;

import java.util.List;

public record SendMessageResponse(
        ChatMessage userMessage,
        ChatMessage assistantMessage,
        List<ChatMessage> messages
) {
}
