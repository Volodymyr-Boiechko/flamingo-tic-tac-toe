package com.flamingo.engine.api.dto;

import com.flamingo.engine.domain.Game;
import com.flamingo.engine.domain.GameStatus;
import com.flamingo.engine.domain.Player;

import java.util.List;


public record GameStateResponse(
        String gameId,
        List<List<String>> board,
        GameStatus status,
        Player nextPlayer
) {

    public static GameStateResponse from(Game game) {
        return new GameStateResponse(
                game.getId(),
                game.getBoard().snapshot(),
                game.getStatus(),
                game.getNextPlayer()
        );
    }
}
