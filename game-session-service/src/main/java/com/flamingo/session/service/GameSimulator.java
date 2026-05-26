package com.flamingo.session.service;

import com.flamingo.session.api.dto.MoveDto;
import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.api.dto.SimulationEventType;
import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.domain.Move;
import com.flamingo.session.domain.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.nonNull;

@Component
public class GameSimulator {

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
        context.events().tryEmitNext(new SimulationEvent(
                SimulationEventType.GAME_FINISHED, null, finalStatus, null));
        context.events().tryEmitComplete();
        registry.remove(session.getSessionId());
    }

    private void failSession(Session session, SimulationContext context, Throwable err) {
        context.events().tryEmitNext(new SimulationEvent(
                SimulationEventType.ERROR, null, null, err.getMessage()));
        context.events().tryEmitError(err);
        registry.remove(session.getSessionId());
    }

    private List<List<String>> emptyBoard() {
        var row = Arrays.asList((String) null, null, null);
        return List.of(row, row, row);
    }
}
