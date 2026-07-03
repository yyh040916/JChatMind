package com.kama.myjchatmind.controller;

import com.kama.myjchatmind.model.ChatSession;
import com.kama.myjchatmind.model.request.CreateSessionRequest;
import com.kama.myjchatmind.service.ChatSessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;

    public ChatSessionController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    @PostMapping
    public ChatSession createSession(@RequestBody CreateSessionRequest request) {
        return chatSessionService.createSession(request);
    }

    @GetMapping
    public List<ChatSession> listSessions() {
        return chatSessionService.listSessions();
    }
}
