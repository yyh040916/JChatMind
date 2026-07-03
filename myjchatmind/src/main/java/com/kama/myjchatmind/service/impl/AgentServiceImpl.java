package com.kama.myjchatmind.service.impl;

import com.kama.myjchatmind.model.Agent;
import com.kama.myjchatmind.model.request.CreateAgentRequest;
import com.kama.myjchatmind.repository.InMemoryStore;
import com.kama.myjchatmind.service.AgentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgentServiceImpl implements AgentService {

    private final InMemoryStore store;
    private final String defaultSystemPrompt;

    public AgentServiceImpl(
            InMemoryStore store,
            @Value("${myjchatmind.default-system-prompt}") String defaultSystemPrompt
    ) {
        this.store = store;
        this.defaultSystemPrompt = defaultSystemPrompt;
    }

    @Override
    public Agent createAgent(CreateAgentRequest request) {
        String name = StringUtils.hasText(request.name()) ? request.name() : "默认助手";
        String systemPrompt = StringUtils.hasText(request.systemPrompt())
                ? request.systemPrompt()
                : defaultSystemPrompt;
        String model = StringUtils.hasText(request.model()) ? request.model() : "deepseek-chat";

        return store.saveAgent(new Agent(
                UUID.randomUUID().toString(),
                name,
                systemPrompt,
                model,
                LocalDateTime.now()
        ));
    }

    @Override
    public List<Agent> listAgents() {
        return store.listAgents();
    }

    @Override
    public Agent getAgent(String agentId) {
        return store.findAgent(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));
    }
}
