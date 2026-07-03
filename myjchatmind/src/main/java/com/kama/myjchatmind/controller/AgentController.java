package com.kama.myjchatmind.controller;

import com.kama.myjchatmind.model.Agent;
import com.kama.myjchatmind.model.request.CreateAgentRequest;
import com.kama.myjchatmind.service.AgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public Agent createAgent(@RequestBody CreateAgentRequest request) {
        return agentService.createAgent(request);
    }

    @GetMapping
    public List<Agent> listAgents() {
        return agentService.listAgents();
    }
}
