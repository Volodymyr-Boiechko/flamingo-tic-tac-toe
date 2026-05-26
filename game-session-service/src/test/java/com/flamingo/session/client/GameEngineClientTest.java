package com.flamingo.session.client;

import com.flamingo.session.client.dto.GameStatusValue;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.client.dto.Position;
import com.flamingo.session.exception.EngineCommunicationException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class GameEngineClientTest {

    private WireMockServer wireMock;
    private GameEngineClient client;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
        client = new GameEngineClient(
                WebClient.builder().baseUrl("http://localhost:" + wireMock.port()).build());
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void createGameReturns201WithParsedResponse() {
        wireMock.stubFor(post(urlEqualTo("/games"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "gameId": "game-1",
                                  "board": [[null,null,null],[null,null,null],[null,null,null]],
                                  "status": "IN_PROGRESS",
                                  "nextPlayer": "X"
                                }
                                """)));

        StepVerifier.create(client.createGame())
                .assertNext(response -> {
                    assertThat(response.gameId()).isEqualTo("game-1");
                    assertThat(response.status()).isEqualTo(GameStatusValue.IN_PROGRESS);
                    assertThat(response.nextPlayer()).isEqualTo(PlayerValue.X);
                    assertThat(response.board()).hasSize(3);
                    assertThat(response.board().getFirst()).containsExactly(null, null, null);
                })
                .verifyComplete();
    }

    @Test
    void createGameMaps500ToEngineCommunicationException() {
        wireMock.stubFor(post(urlEqualTo("/games"))
                .willReturn(aResponse().withStatus(500).withBody("internal error")));

        StepVerifier.create(client.createGame())
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(EngineCommunicationException.class);
                    assertThat(t.getMessage()).contains("500");
                })
                .verify();
    }

    @Test
    void createGameConnectionRefusedMapsToEngineCommunicationException() {
        var brokenClient = new GameEngineClient(
                WebClient.builder().baseUrl("http://localhost:1").build());

        StepVerifier.create(brokenClient.createGame())
                .expectError(EngineCommunicationException.class)
                .verify();
    }

    @Test
    void makeMoveVerifiesRequestBodyAndParsesResponse() {
        wireMock.stubFor(post(urlEqualTo("/games/abc-123/move"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "gameId": "abc-123",
                                  "board": [["X",null,null],[null,null,null],[null,null,null]],
                                  "status": "IN_PROGRESS",
                                  "nextPlayer": "O"
                                }
                                """)));

        StepVerifier.create(client.makeMove("abc-123", PlayerValue.X, new Position(0, 0)))
                .assertNext(response -> {
                    assertThat(response.gameId()).isEqualTo("abc-123");
                    assertThat(response.status()).isEqualTo(GameStatusValue.IN_PROGRESS);
                    assertThat(response.nextPlayer()).isEqualTo(PlayerValue.O);
                    assertThat(response.board().getFirst()).containsExactly("X", null, null);
                })
                .verifyComplete();

        verify(postRequestedFor(urlEqualTo("/games/abc-123/move"))
                .withRequestBody(equalToJson("""
                        {
                          "player": "X",
                          "position": {"row": 0, "col": 0}
                        }
                        """)));
    }

    @Test
    void makeMove400MapsToEngineCommunicationException() {
        wireMock.stubFor(post(urlEqualTo("/games/game-1/move"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"title":"Invalid Move","status":400}
                                """)));

        StepVerifier.create(client.makeMove("game-1", PlayerValue.O, new Position(0, 0)))
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(EngineCommunicationException.class);
                    assertThat(t.getMessage()).contains("400");
                })
                .verify();
    }

    @Test
    void makeMove404MapsToEngineCommunicationException() {
        wireMock.stubFor(post(urlEqualTo("/games/missing/move"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"title":"Game Not Found","status":404}
                                """)));

        StepVerifier.create(client.makeMove("missing", PlayerValue.X, new Position(1, 1)))
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(EngineCommunicationException.class);
                    assertThat(t.getMessage()).contains("404");
                })
                .verify();
    }

    @Test
    void makeMove409MapsToEngineCommunicationException() {
        wireMock.stubFor(post(urlEqualTo("/games/done/move"))
                .willReturn(aResponse()
                        .withStatus(409)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"title":"Game Already Finished","status":409}
                                """)));

        StepVerifier.create(client.makeMove("done", PlayerValue.X, new Position(2, 2)))
                .expectErrorSatisfies(t -> {
                    assertThat(t).isInstanceOf(EngineCommunicationException.class);
                    assertThat(t.getMessage()).contains("409");
                })
                .verify();
    }

    @Test
    void makeMoveConnectionRefusedMapsToEngineCommunicationException() {
        var brokenClient = new GameEngineClient(
                WebClient.builder().baseUrl("http://localhost:1").build());

        StepVerifier.create(brokenClient.makeMove("game-1", PlayerValue.X, new Position(0, 0)))
                .expectError(EngineCommunicationException.class)
                .verify();
    }
}
