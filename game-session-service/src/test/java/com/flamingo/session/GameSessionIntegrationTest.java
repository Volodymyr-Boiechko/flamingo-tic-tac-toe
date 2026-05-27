package com.flamingo.session;

import com.flamingo.session.api.dto.CreateSessionResponse;
import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.api.dto.SimulationEventType;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameSessionIntegrationTest {

    static WireMockServer wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        wireMock.start();
    }

    @DynamicPropertySource
    static void engineProps(DynamicPropertyRegistry registry) {
        registry.add("game-engine.url", () -> "http://localhost:" + wireMock.port());
        registry.add("simulation.move-delay-ms", () -> "10");
    }

    @Autowired
    WebTestClient webTestClient;

    @BeforeEach
    void resetStubs() {
        wireMock.resetAll();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    // ── Happy path ─────────────────────────────────────────

    @Test
    void createSessionCallsEngineAndReturnsSessionId() {
        stubCreateGame();

        var result = webTestClient.post().uri("/sessions")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSessionResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(result);
        assertThat(result.sessionId()).isNotNull();
        assertThat(result.gameId()).isEqualTo("engine-game-1");
        assertThat(result.createdAt()).isNotNull();
        wireMock.verify(1, postRequestedFor(urlEqualTo("/games")));
    }

    @Test
    @DisplayName("Full game flow: create session, run simulation, verify FINISHED status")
    void simulateCompletesFullGameFlow() {
        stubCreateGame();
        stubMoveReturnsXWon();

        var sessionId = createSession();

        webTestClient.post().uri("/sessions/{id}/simulate", sessionId)
                .exchange()
                .expectStatus().isAccepted();

        // Block until the SSE stream ends (sink completes when game finishes)
        webTestClient.get().uri("/sessions/{id}/events", sessionId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(SimulationEvent.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(5));

        webTestClient.get().uri("/sessions/{id}", sessionId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FINISHED")
                .jsonPath("$.moves[0]").exists();
    }

    @Test
    @DisplayName("SSE stream emits MOVE_MADE events and final GAME_FINISHED via replay sink")
    void streamEventsReceivesSimulationProgress() {
        stubCreateGame();
        stubMoveReturnsXWon();

        var sessionId = createSession();

        webTestClient.post().uri("/sessions/{id}/simulate", sessionId)
                .exchange()
                .expectStatus().isAccepted();

        var events = webTestClient.get().uri("/sessions/{id}/events", sessionId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(SimulationEvent.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(events).isNotEmpty();
        assertThat(events).anyMatch(e -> e.type() == SimulationEventType.MOVE_MADE);
        assertThat(events).filteredOn(e -> e.type() == SimulationEventType.GAME_FINISHED).hasSize(1);
        assertThat(events.getLast().type()).isEqualTo(SimulationEventType.GAME_FINISHED);
    }

    // ── Error responses ────────────────────────────────────

    @Test
    void createSessionReturns502WhenEngineDown() {
        wireMock.stubFor(post(urlEqualTo("/games"))
                .willReturn(aResponse().withStatus(500).withBody("internal error")));

        webTestClient.post().uri("/sessions")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.BAD_GATEWAY)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Engine Communication Failed");
    }

    @Test
    void getSessionReturns404WhenIdUnknown() {
        webTestClient.get().uri("/sessions/nonexistent-id")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Session Not Found");
    }

    @Test
    void streamEventsReturns404WhenSessionUnknown() {
        webTestClient.get().uri("/sessions/unknown-id/events")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void simulateReturns409WhenSessionAlreadyFinished() {
        stubCreateGame();
        stubMoveReturnsXWon();

        var sessionId = createSession();

        webTestClient.post().uri("/sessions/{id}/simulate", sessionId)
                .exchange()
                .expectStatus().isAccepted();

        webTestClient.get().uri("/sessions/{id}/events", sessionId)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(SimulationEvent.class)
                .getResponseBody()
                .collectList()
                .block(Duration.ofSeconds(5));

        webTestClient.post().uri("/sessions/{id}/simulate", sessionId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Session Already Finished");
    }

    @Test
    @DisplayName("Second POST /simulate while first is still running returns 409 Session Already Running")
    void simulateReturns409WhenSessionAlreadyRunning() {
        stubCreateGame();
        // Engine never responds — simulation stays in SIMULATING state indefinitely
        wireMock.stubFor(post(urlPathMatching("/games/.*/move"))
                .willReturn(aResponse().withFixedDelay(60_000).withStatus(200)));

        var sessionId = createSession();

        webTestClient.post().uri("/sessions/{id}/simulate", sessionId)
                .exchange()
                .expectStatus().isAccepted();

        // The CREATED → SIMULATING transition is synchronous; 202 already returned means done
        webTestClient.post().uri("/sessions/{id}/simulate", sessionId)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Session Already Running");
    }

    // ── helpers ───────────────────────────────────────────

    private String createSession() {
        return webTestClient.post().uri("/sessions")
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CreateSessionResponse.class)
                .returnResult()
                .getResponseBody()
                .sessionId();
    }

    private void stubCreateGame() {
        wireMock.stubFor(post(urlEqualTo("/games"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "gameId": "engine-game-1",
                                  "board": [[null,null,null],[null,null,null],[null,null,null]],
                                  "status": "IN_PROGRESS",
                                  "nextPlayer": "X"
                                }
                                """)));
    }

    private void stubMoveReturnsXWon() {
        wireMock.stubFor(post(urlPathMatching("/games/.*/move"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "gameId": "engine-game-1",
                                  "board": [["X","X","X"],[null,null,null],[null,null,null]],
                                  "status": "X_WON",
                                  "nextPlayer": null
                                }
                                """)));
    }
}
