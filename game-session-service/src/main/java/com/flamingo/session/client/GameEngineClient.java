package com.flamingo.session.client;

import com.flamingo.session.client.dto.CreateGameResponse;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.MoveRequest;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.client.dto.Position;
import com.flamingo.session.exception.EngineCommunicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Non-blocking HTTP client for the Game Engine Service. Wraps every error
 * (HTTP 4xx/5xx, connection failure, timeout) into
 * {@link EngineCommunicationException}, giving callers a single exception type
 * to handle for any cross-service failure.
 *
 * <p>Uses Spring's {@link WebClient} for fully reactive request/response;
 * no thread is blocked during the call.
 */
@Component
public class GameEngineClient {

    private static final Logger log = LoggerFactory.getLogger(GameEngineClient.class);

    private final WebClient webClient;

    public GameEngineClient(WebClient gameEngineWebClient) {
        this.webClient = gameEngineWebClient;
    }

    public Mono<CreateGameResponse> createGame() {
        return webClient.post()
                .uri("/games")
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(CreateGameResponse.class)
                .onErrorMap(this::wrapNonEngineError);
    }

    public Mono<GameStateResponse> makeMove(String gameId, PlayerValue player, Position position) {
        var request = new MoveRequest(player, position);
        return webClient.post()
                .uri("/games/{id}/move", gameId)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .bodyToMono(GameStateResponse.class)
                .onErrorMap(this::wrapNonEngineError);
    }

    private Mono<? extends Throwable> mapError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .map(body -> new EngineCommunicationException(
                        "engine returned " + response.statusCode().value() + ": " + body));
    }

    private Throwable wrapNonEngineError(Throwable t) {
        if (t instanceof EngineCommunicationException) {
            return t;
        }
        log.warn("Engine communication failed: {}", t.getMessage());
        return new EngineCommunicationException("failed to communicate with engine", t);
    }
}
