package com.flamingo.session.service;

import com.flamingo.session.client.GameEngineClient;
import com.flamingo.session.client.dto.CreateGameResponse;
import com.flamingo.session.client.dto.GameStatusValue;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.domain.Session;
import com.flamingo.session.domain.SessionStatus;
import com.flamingo.session.exception.EngineCommunicationException;
import com.flamingo.session.exception.SessionAlreadyFinishedException;
import com.flamingo.session.exception.SessionAlreadyRunningException;
import com.flamingo.session.exception.SessionNotFoundException;
import com.flamingo.session.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock
    SessionRepository repository;
    @Mock
    GameEngineClient engineClient;
    @Mock
    GameSimulator simulator;

    SessionService sessionService;

    @BeforeEach
    void setUp() {
        sessionService = new SessionService(repository, engineClient, simulator);
    }

    @Test
    void createSessionCallsEngineAndSavesSession() {
        // Given
        when(engineClient.createGame()).thenReturn(
                Mono.just(new CreateGameResponse("game-1", List.of(), GameStatusValue.IN_PROGRESS, PlayerValue.X)));

        // When / Then
        StepVerifier.create(sessionService.createSession())
                .assertNext(session -> {
                    assertThat(session.getGameId()).isEqualTo("game-1");
                    assertThat(session.getSessionId()).isNotNull();
                    assertThat(session.getStatus()).isEqualTo(SessionStatus.CREATED);
                })
                .verifyComplete();

        verify(repository).save(any(Session.class));
    }

    @Test
    void createSessionPropagatesEngineFailure() {
        when(engineClient.createGame()).thenReturn(
                Mono.error(new EngineCommunicationException("engine down")));

        StepVerifier.create(sessionService.createSession())
                .expectError(EngineCommunicationException.class)
                .verify();
    }

    @Test
    void getSessionReturnsSessionWhenExists() {
        var session = new Session("session-1", "game-1");
        when(repository.findById("session-1")).thenReturn(Optional.of(session));

        StepVerifier.create(sessionService.getSession("session-1"))
                .assertNext(s -> assertThat(s.getSessionId()).isEqualTo("session-1"))
                .verifyComplete();
    }

    @Test
    void getSessionThrowsWhenIdUnknown() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        StepVerifier.create(sessionService.getSession("missing"))
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(SessionNotFoundException.class);
                    assertThat(t.getMessage()).contains("missing");
                })
                .verify();
    }

    @Test
    @DisplayName("startSimulation returns immediately without waiting for simulation to finish")
    void startSimulationFiresAndForgetsReturnsImmediately() {
        // Given
        var session = new Session("session-1", "game-1");
        when(repository.findById("session-1")).thenReturn(Optional.of(session));
        when(simulator.simulate(session)).thenReturn(Mono.never());

        // When / Then completes immediately even though simulation never finishes
        StepVerifier.create(sessionService.startSimulation("session-1"))
                .verifyComplete();

        verify(simulator).simulate(session);
    }

    @Test
    void startSimulationThrowsWhenSessionAlreadyFinished() {
        // Given
        var session = new Session("session-1", "game-1");
        session.markFinished();
        when(repository.findById("session-1")).thenReturn(Optional.of(session));

        // When / Then
        StepVerifier.create(sessionService.startSimulation("session-1"))
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(SessionAlreadyFinishedException.class);
                    assertThat(t.getMessage()).contains("session-1");
                })
                .verify();

        verifyNoInteractions(simulator);
    }

    @Test
    @DisplayName("startSimulation rejects concurrent call when session is already simulating")
    void startSimulationThrowsWhenSessionAlreadyRunning() {
        // Given — transition session to SIMULATING as a first call would
        var session = new Session("session-1", "game-1");
        session.startSimulationIfNotStarted();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.SIMULATING);
        when(repository.findById("session-1")).thenReturn(Optional.of(session));

        // When / Then
        StepVerifier.create(sessionService.startSimulation("session-1"))
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(SessionAlreadyRunningException.class);
                    assertThat(t.getMessage()).contains("session-1");
                })
                .verify();

        verifyNoInteractions(simulator);
    }
}
