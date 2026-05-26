package com.flamingo.session.service;

import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.domain.Session;
import com.flamingo.session.repository.SessionRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final SessionRepository repository;
    private final GameEngineClient engineClient;
    private final GameSimulator simulator;

    public SessionService(SessionRepository repository, GameEngineClient engineClient, GameSimulator simulator) {
        this.repository = repository;
        this.engineClient = engineClient;
        this.simulator = simulator;
    }

    public Session createSession() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void startSimulation(String sessionId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Session getSession(String sessionId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
