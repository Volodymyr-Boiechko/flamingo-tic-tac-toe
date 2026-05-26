package com.flamingo.session.client;

import com.flamingo.session.client.dto.CreateGameResponse;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.MoveRequest;
import com.flamingo.session.client.dto.PlayerValue;
import com.flamingo.session.client.dto.Position;
import com.flamingo.session.exception.EngineCommunicationException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GameEngineClient {

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
        return new EngineCommunicationException("failed to communicate with engine", t);
    }
}
