package com.kama.myjchatmind.controller;

import com.kama.myjchatmind.model.ChatMessage;
import com.kama.myjchatmind.model.request.SendMessageRequest;
import com.kama.myjchatmind.model.response.SendMessageResponse;
import com.kama.myjchatmind.service.ChatMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/send")
    public SendMessageResponse sendMessage(@RequestBody SendMessageRequest request) {
        return chatMessageService.sendMessage(request);
    }

    @GetMapping("/{sessionId}")
    public List<ChatMessage> listMessages(@PathVariable String sessionId) {
        return chatMessageService.listMessages(sessionId);
    }
}
