package com.flamingo.session.service;

import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.domain.Session;
import com.flamingo.session.domain.SessionStatus;
import com.flamingo.session.exception.SessionAlreadyFinishedException;
import com.flamingo.session.exception.SessionAlreadyRunningException;
import com.flamingo.session.exception.SessionNotFoundException;
import com.flamingo.session.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

/**
 * Orchestrates session lifecycle and simulation triggering. Sessions are stored
 * in-memory and bound to a backend game (managed by Game Engine Service) at
 * creation time.
 *
 * <p>Simulation is fire-and-forget: {@link #startSimulation} returns immediately
 * with the simulation running asynchronously on {@link Schedulers#parallel()}.
 * Lifecycle events are streamed via {@link GameSimulator#eventStream(String)} for
 * SSE consumers.
 *
 * <p>Concurrent simulate calls on the same session are guarded by an atomic
 * CREATED → SIMULATING transition; the second call is rejected with
 * {@link SessionAlreadyRunningException}.
 */
@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

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

    /**
     * Creates a new session backed by a freshly created Engine game.
     *
     * @return a {@link Mono} that emits the persisted session on success, or signals
     * {@code EngineCommunicationException} if the engine call fails.
     */
    public Mono<Session> createSession() {
        return engineClient.createGame()
                .map(game -> {
                    var sessionId = UUID.randomUUID().toString();
                    var session = new Session(sessionId, game.gameId());
                    repository.save(session);
                    log.info("Created session {} backed by game {}", sessionId, game.gameId());
                    return session;
                });
    }

    /**
     * Triggers asynchronous simulation for an existing session. Returns immediately
     * (HTTP 202 semantics). The actual simulation runs on {@link Schedulers#parallel()}
     * and emits events through the SSE sink.
     *
     * @throws SessionNotFoundException        if no session matches the id
     * @throws SessionAlreadyFinishedException if the session has already finished or failed
     * @throws SessionAlreadyRunningException  if a simulation is already in progress
     */
    public Mono<Void> startSimulation(String sessionId) {
        return getSession(sessionId)
                .doOnNext(this::startSimulationAsync)
                .then();
    }

    private void startSimulationAsync(Session session) {
        if (!session.startSimulationIfNotStarted()) {
            var status = session.getStatus();
            if (status == SessionStatus.FINISHED || status == SessionStatus.FAILED) {
                throw new SessionAlreadyFinishedException(
                        "session already finished: " + session.getSessionId());
            }
            throw new SessionAlreadyRunningException(
                    "session simulation already running: " + session.getSessionId());
        }

        log.info("Starting simulation for session {} (game {})", session.getSessionId(), session.getGameId());

        simulator.simulate(session)
                .subscribeOn(Schedulers.parallel())
                .subscribe(null, err -> log.error(
                        "Simulation failed for session {}", session.getSessionId(), err));
    }

    public Mono<Session> getSession(String sessionId) {
        return Mono.fromCallable(() -> repository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("session not found: " + sessionId)));
    }

    /**
     * Returns the live SSE event stream for an existing session.
     *
     * @throws SessionNotFoundException if no session matches the id
     */
    public Flux<SimulationEvent> streamEvents(String sessionId) {
        return getSession(sessionId)
                .thenMany(simulator.eventStream(sessionId));
    }
}
