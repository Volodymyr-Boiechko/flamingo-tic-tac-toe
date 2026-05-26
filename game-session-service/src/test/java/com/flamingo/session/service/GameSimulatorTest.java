package com.flamingo.session.service;

import com.flamingo.session.api.dto.SimulationEventType;
import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.GameStatusValue;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.client.dto.Position;
import com.flamingo.session.domain.Session;
import com.flamingo.session.exception.EngineCommunicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameSimulatorTest {

    @Mock GameEngineClient engineClient;
    @Mock MoveGenerator moveGenerator;

    SimulationContextRegistry registry;
    GameSimulator simulator;

    @BeforeEach
    void setUp() {
        registry = new SimulationContextRegistry();
        simulator = new GameSimulator(engineClient, moveGenerator, registry, 0L);
    }

    // ── Move loop ──────────────────────────────────────────

    @Test
    void simulateMakesMovesUntilEngineReportsFinished() {
        // Given
        var board = emptyBoard();
        var inProgress1 = new GameStateResponse("game-1", board, GameStatusValue.IN_PROGRESS, PlayerValue.O);
        var inProgress2 = new GameStateResponse("game-1", board, GameStatusValue.IN_PROGRESS, PlayerValue.X);
        var xWon = new GameStateResponse("game-1", board, GameStatusValue.X_WON, null);
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any()))
                .thenReturn(Mono.just(inProgress1))
                .thenReturn(Mono.just(inProgress2))
                .thenReturn(Mono.just(xWon));
        var session = new Session("session-1", "game-1");

        // When / Then
        StepVerifier.create(simulator.simulate(session))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        verify(engineClient, times(3)).makeMove(eq("game-1"), any(), any());
    }

    @Test
    void simulateEmitsMoveMadeEventForEachMove() {
        // Given
        var board = emptyBoard();
        var inProgress = new GameStateResponse("game-1", board, GameStatusValue.IN_PROGRESS, PlayerValue.O);
        var xWon = new GameStateResponse("game-1", board, GameStatusValue.X_WON, null);
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any()))
                .thenReturn(Mono.just(inProgress))
                .thenReturn(Mono.just(xWon));
        var session = new Session("session-1", "game-1");
        var events = simulator.eventStream("session-1");

        // When / Then
        StepVerifier.create(events)
                .then(() -> simulator.simulate(session).subscribe())
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE && e.move().player() == PlayerValue.X)
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE && e.move().player() == PlayerValue.O)
                .expectNextMatches(e -> e.type() == SimulationEventType.GAME_FINISHED)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    void simulateEmitsGameFinishedEventOnCompletion() {
        // Given
        var xWon = new GameStateResponse("game-1", emptyBoard(), GameStatusValue.X_WON, null);
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any())).thenReturn(Mono.just(xWon));
        var session = new Session("session-1", "game-1");
        var events = simulator.eventStream("session-1");

        // When / Then
        StepVerifier.create(events)
                .then(() -> simulator.simulate(session).subscribe())
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE)
                .expectNextMatches(e ->
                        e.type() == SimulationEventType.GAME_FINISHED
                        && e.status() == GameStatusValue.X_WON)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    // ── Error handling ─────────────────────────────────────

    @Test
    void simulateEmitsErrorEventOnEngineFailure() {
        // Given
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any()))
                .thenReturn(Mono.error(new EngineCommunicationException("engine down")));
        var session = new Session("session-1", "game-1");
        var events = simulator.eventStream("session-1");

        // When / Then
        StepVerifier.create(events)
                .then(() -> simulator.simulate(session).subscribe(e -> {}, err -> {}))
                .assertNext(e -> {
                    assertThat(e.type()).isEqualTo(SimulationEventType.ERROR);
                    assertThat(e.message()).isNotNull();
                })
                .expectError(EngineCommunicationException.class)
                .verify(Duration.ofSeconds(5));
    }

    // ── Replay semantics ───────────────────────────────────

    @Test
    @DisplayName("Subscriber connecting after simulation completes still receives all events via replay sink")
    void lateSubscriberReceivesAllReplayedEvents() {
        // Given
        var board = emptyBoard();
        var inProgress = new GameStateResponse("game-1", board, GameStatusValue.IN_PROGRESS, PlayerValue.O);
        var xWon = new GameStateResponse("game-1", board, GameStatusValue.X_WON, null);
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any()))
                .thenReturn(Mono.just(inProgress))
                .thenReturn(Mono.just(xWon));
        var session = new Session("session-1", "game-1");

        // Capture flux reference before simulation (creates the context in the registry)
        var events = simulator.eventStream("session-1");

        // When run to completion; context is removed from registry after this
        StepVerifier.create(simulator.simulate(session))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // Then subscribing to the captured flux still yields all buffered events
        StepVerifier.create(events)
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE)
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE)
                .expectNextMatches(e -> e.type() == SimulationEventType.GAME_FINISHED && e.status() == GameStatusValue.X_WON)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("All subscribers on the same sink receive identical event sequences")
    void multipleSubscribersReceiveSameEvents() {
        // Given
        var xWon = new GameStateResponse("game-1", emptyBoard(), GameStatusValue.X_WON, null);
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any())).thenReturn(Mono.just(xWon));
        var session = new Session("session-1", "game-1");
        var events1 = simulator.eventStream("session-1");
        var events2 = simulator.eventStream("session-1");

        // When / Then first subscriber triggers simulation
        StepVerifier.create(events1)
                .then(() -> simulator.simulate(session).subscribe())
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE)
                .expectNextMatches(e -> e.type() == SimulationEventType.GAME_FINISHED)
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // Second subscriber receives the same replayed events
        StepVerifier.create(events2)
                .expectNextMatches(e -> e.type() == SimulationEventType.MOVE_MADE)
                .expectNextMatches(e -> e.type() == SimulationEventType.GAME_FINISHED)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    // ── Cleanup ────────────────────────────────────────────

    @Test
    void registryIsCleanedUpAfterSimulationFinishes() {
        // Given
        var xWon = new GameStateResponse("game-1", emptyBoard(), GameStatusValue.X_WON, null);
        when(moveGenerator.nextMove(any())).thenReturn(new Position(0, 0));
        when(engineClient.makeMove(any(), any(), any())).thenReturn(Mono.just(xWon));
        var session = new Session("session-1", "game-1");

        // When
        StepVerifier.create(simulator.simulate(session))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // Then
        assertThat(registry.find("session-1")).isEmpty();
    }

    private List<List<String>> emptyBoard() {
        var row = Arrays.asList((String) null, null, null);
        return List.of(row, row, row);
    }
}
