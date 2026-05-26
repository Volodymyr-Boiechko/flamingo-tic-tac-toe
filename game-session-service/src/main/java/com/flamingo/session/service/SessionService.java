package com.flamingo.session.service;

import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.domain.Session;
import com.flamingo.session.domain.SessionStatus;
import com.flamingo.session.exception.SessionAlreadyFinishedException;
import com.flamingo.session.exception.SessionNotFoundException;
import com.flamingo.session.repository.SessionRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Service
public class SessionService {

    private final SessionRepository repository;
    private final GameEngineClient engineClient;
    private final GameSimulator simulator;

    public SessionService(SessionRepository repository,
                          GameEngineClient engineClient,
                          GameSimulator simulator) {
        this.repository = repository;
        this.engineClient = engineClient;
        this.simulator = simulator;
    }

    public Mono<Session> createSession() {
        return engineClient.createGame()
                .map(game -> {
                    var sessionId = UUID.randomUUID().toString();
                    var session = new Session(sessionId, game.gameId());
                    repository.save(session);
                    return session;
                });
    }

    public Mono<Void> startSimulation(String sessionId) {
        return getSession(sessionId)
                .doOnNext(session -> {
                    if (session.getStatus() == SessionStatus.FINISHED) {
                        throw new SessionAlreadyFinishedException("session already finished: " + sessionId);
                    }
                    simulator.simulate(session)
                            .subscribeOn(Schedulers.parallel())
                            .subscribe();
                })
                .then();
    }

    public Mono<Session> getSession(String sessionId) {
        return Mono.fromCallable(() -> repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("session not found: " + sessionId)));
    }
}
