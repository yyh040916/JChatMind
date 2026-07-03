package com.kama.myjchatmind.agent;

import com.kama.myjchatmind.config.ChatClientRegistry;
import com.kama.myjchatmind.repository.InMemoryStore;
import com.kama.myjchatmind.service.AgentService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SimpleAgentFactory {

    private final AgentService agentService;
    private final InMemoryStore store;
    private final ChatClientRegistry chatClientRegistry;

    public SimpleAgentFactory(
            AgentService agentService,
            InMemoryStore store,
            ChatClientRegistry chatClientRegistry
    ) {
        this.agentService = agentService;
        this.store = store;
        this.chatClientRegistry = chatClientRegistry;
    }

    public SimpleAgent create(String agentId, String sessionId) {
        var agent = agentService.getAgent(agentId);
        ChatClient chatClient = chatClientRegistry.get(agent.getModel());
        if (chatClient == null) {
            throw new IllegalStateException("No ChatClient found for model: " + agent.getModel());
        }
        return new SimpleAgent(agent, sessionId, chatClient, store);
    }
}
