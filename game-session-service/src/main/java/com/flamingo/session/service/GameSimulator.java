package com.flamingo.session.service;

import com.flamingo.session.api.dto.MoveDto;
import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.api.dto.SimulationEventType;
import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.domain.Move;
import com.flamingo.session.domain.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 * Drives the move-by-move simulation of a Tic Tac Toe game by calling the Game
 * Engine Service through {@link GameEngineClient}.
 *
 * <p>The simulation is a reactive pipeline:
 * <pre>
 *   Mono.defer(playOneMove)
 *       .delayElement(moveDelayMs)
 *       .repeat()
 *       .takeUntil(state.status().isFinished())
 * </pre>
 *
 * <p>Each move emission triggers a {@code MOVE_MADE} event on a per-session
 * {@link reactor.core.publisher.Sinks.Many} replay sink, allowing late SSE
 * subscribers to catch up on the full event history. The final
 * {@code GAME_FINISHED} event is emitted before the sink completes.
 *
 * <p>Per-session state (sink + current game snapshot) lives in
 * {@link SimulationContextRegistry} and is removed when the simulation ends.
 */
@Component
public class GameSimulator {

    private static final Logger log = LoggerFactory.getLogger(GameSimulator.class);

    private final GameEngineClient engineClient;
    private final MoveGenerator moveGenerator;
    private final SimulationContextRegistry registry;
    private final long moveDelayMs;

    public GameSimulator(GameEngineClient engineClient,
                         MoveGenerator moveGenerator,
                         SimulationContextRegistry registry,
                         @Value("${simulation.move-delay-ms:400}") long moveDelayMs) {
        this.engineClient = engineClient;
        this.moveGenerator = moveGenerator;
        this.registry = registry;
        this.moveDelayMs = moveDelayMs;
    }

    public Mono<Void> simulate(Session session) {
        var context = registry.getOrCreate(session.getSessionId());
        log.debug("Starting simulation loop for session {}", session.getSessionId());

        return Mono.defer(() -> playOneMove(session, context))
                .delayElement(Duration.ofMillis(moveDelayMs))
                .repeat()
                .takeUntil(state -> state.status().isFinished())
                .doOnComplete(() -> finishSession(session, context))
                .doOnError(err -> failSession(session, context, err))
                .then();
    }

    public Flux<SimulationEvent> eventStream(String sessionId) {
        return registry.find(sessionId)
                .map(ctx -> ctx.events().asFlux())
                .orElseGet(() -> registry.getOrCreate(sessionId).events().asFlux());
    }

    private Mono<GameStateResponse> playOneMove(Session session, SimulationContext context) {
        var current = context.currentState();
        var board = nonNull(current) ? current.board() : emptyBoard();
        var player = nonNull(current) ? current.nextPlayer() : PlayerValue.X;
        var move = moveGenerator.nextMove(board);

        return engineClient.makeMove(session.getGameId(), player, move)
                .doOnNext(state -> {
                    context.updateState(state);
                    session.recordMove(new Move(player, move.row(), move.col(), Instant.now()));
                    context.events().tryEmitNext(new SimulationEvent(
                            SimulationEventType.MOVE_MADE,
                            new MoveDto(player, move.row(), move.col(), Instant.now()),
                            null, null));
                });
    }

    private void finishSession(Session session, SimulationContext context) {
        session.markFinished();
        var finalStatus = context.currentState().status();
        log.info("Simulation finished for session {} with status {}", session.getSessionId(), finalStatus);
        context.events().tryEmitNext(new SimulationEvent(
                SimulationEventType.GAME_FINISHED, null, finalStatus, null));
        context.events().tryEmitComplete();
        registry.remove(session.getSessionId());
    }

    private void failSession(Session session, SimulationContext context, Throwable err) {
        log.warn("Simulation failed for session {}: {}", session.getSessionId(), err.getMessage());
        session.markFailed();
        context.events().tryEmitNext(new SimulationEvent(
                SimulationEventType.ERROR, null, null, err.getMessage()));
        context.events().tryEmitError(err);
        registry.remove(session.getSessionId());
    }

    private List<List<String>> emptyBoard() {
        return List.of(
                Arrays.asList(null, null, null),
                Arrays.asList(null, null, null),
                Arrays.asList(null, null, null)
        );
    }
}
