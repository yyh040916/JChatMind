package com.kama.myjchatmind.service;

import com.kama.myjchatmind.model.ChatSession;
import com.kama.myjchatmind.model.request.CreateSessionRequest;

import java.util.List;

public interface ChatSessionService {

    ChatSession createSession(CreateSessionRequest request);

    List<ChatSession> listSessions();

    ChatSession getSession(String sessionId);
}
