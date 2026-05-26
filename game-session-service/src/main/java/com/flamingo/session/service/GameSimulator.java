package com.flamingo.session.service;

import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.repository.SessionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class GameSimulator {

    private final GameEngineClient engineClient;
    private final MoveGenerator moveGenerator;
    private final SessionRepository repository;

    public GameSimulator(GameEngineClient engineClient, MoveGenerator moveGenerator, SessionRepository repository) {
        this.engineClient = engineClient;
        this.moveGenerator = moveGenerator;
        this.repository = repository;
    }

    @Async
    public void simulate(String sessionId, String gameId) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Flux<SimulationEvent> eventStream(String sessionId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
