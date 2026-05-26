package com.flamingo.session.client;

import com.flamingo.session.client.dto.CreateGameResponse;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.Position;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GameEngineClient {

    private final WebClient webClient;

    public GameEngineClient(WebClient gameEngineWebClient) {
        this.webClient = gameEngineWebClient;
    }

    public CreateGameResponse createGame() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public GameStateResponse makeMove(String gameId, String player, Position position) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
