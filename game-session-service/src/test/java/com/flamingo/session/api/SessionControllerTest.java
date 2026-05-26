package com.flamingo.session.api;

import com.flamingo.session.api.dto.CreateSessionResponse;
import com.flamingo.session.api.dto.MoveDto;
import com.flamingo.session.api.dto.SessionDetailsResponse;
import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.api.dto.SimulationEventType;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.domain.Session;
import com.flamingo.session.domain.SessionStatus;
import com.flamingo.session.exception.SessionNotFoundException;
import com.flamingo.session.service.GameSimulator;
import com.flamingo.session.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    WebTestClient client;
    @MockitoBean
    SessionService sessionService;
    @MockitoBean
    GameSimulator simulator;

    @Test
    void createSessionReturns201WithBody() {
        var session = new Session("session-1", "game-1");
        when(sessionService.createSession()).thenReturn(Mono.just(session));

        client.post().uri("/sessions")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSessionResponse.class)
                .value(body -> {
                    assertThat(body.sessionId()).isEqualTo("session-1");
                    assertThat(body.gameId()).isEqualTo("game-1");
                    assertThat(body.createdAt()).isNotNull();
                });
    }

    @Test
    void startSimulationReturns202() {
        when(sessionService.startSimulation("session-1")).thenReturn(Mono.empty());

        client.post().uri("/sessions/session-1/simulate")
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    void getSessionReturns200WithDetails() {
        var session = new Session("session-1", "game-1");
        when(sessionService.getSession("session-1")).thenReturn(Mono.just(session));

        client.get().uri("/sessions/session-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(SessionDetailsResponse.class)
                .value(body -> {
                    assertThat(body.sessionId()).isEqualTo("session-1");
                    assertThat(body.status()).isEqualTo(SessionStatus.CREATED);
                    assertThat(body.moves()).isEmpty();
                });
    }

    @Test
    void getSessionReturns404WhenNotFound() {
        when(sessionService.getSession("missing"))
                .thenReturn(Mono.error(new SessionNotFoundException("session not found: missing")));

        client.get().uri("/sessions/missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void streamEventsReturnsTextEventStreamMediaType() {
        when(simulator.eventStream("session-1")).thenReturn(Flux.empty());

        client.get().uri("/sessions/session-1/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }

    @Test
    void streamEventsReturnsAllSimulationEvents() {
        when(simulator.eventStream("session-1")).thenReturn(Flux.just(
                new SimulationEvent(SimulationEventType.MOVE_MADE,
                        new MoveDto(PlayerValue.X, 0, 0, Instant.now()), null, null),
                new SimulationEvent(SimulationEventType.GAME_FINISHED, null, null, null)));

        var events = client.get().uri("/sessions/session-1/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(SimulationEvent.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(events).hasSize(2);
        assertThat(events.get(0).type()).isEqualTo(SimulationEventType.MOVE_MADE);
        assertThat(events.get(1).type()).isEqualTo(SimulationEventType.GAME_FINISHED);
    }
}
