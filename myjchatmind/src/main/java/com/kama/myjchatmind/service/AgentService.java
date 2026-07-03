package com.kama.myjchatmind.service;

import com.kama.myjchatmind.model.Agent;
import com.kama.myjchatmind.model.request.CreateAgentRequest;

import java.util.List;

public interface AgentService {

    Agent createAgent(CreateAgentRequest request);

    List<Agent> listAgents();

    Agent getAgent(String agentId);
}
