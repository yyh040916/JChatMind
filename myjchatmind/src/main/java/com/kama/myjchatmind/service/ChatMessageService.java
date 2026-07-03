package com.kama.myjchatmind.service;

import com.kama.myjchatmind.model.ChatMessage;
import com.kama.myjchatmind.model.request.SendMessageRequest;
import com.kama.myjchatmind.model.response.SendMessageResponse;

import java.util.List;

public interface ChatMessageService {

    SendMessageResponse sendMessage(SendMessageRequest request);

    List<ChatMessage> listMessages(String sessionId);
}
