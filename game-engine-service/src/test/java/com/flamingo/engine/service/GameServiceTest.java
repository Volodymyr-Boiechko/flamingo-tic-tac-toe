package com.flamingo.engine.service;

import com.flamingo.engine.domain.GameStatus;
import com.flamingo.engine.domain.Player;
import com.flamingo.engine.domain.Position;
import com.flamingo.engine.exception.GameNotFoundException;
import com.flamingo.engine.repository.InMemoryGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class GameServiceTest {

    private GameService service;

    @BeforeEach
    void setUp() {
        service = new GameService(new InMemoryGameRepository());
    }

    @Test
    void createGameReturnsNewGameWithUUID() {
        var game = service.createGame();
        assertThat(game).isNotNull();
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThatNoException().isThrownBy(() -> UUID.fromString(game.getId()));
    }

    @Test
    void createGameProducesUniqueIds() {
        var id1 = service.createGame().getId();
        var id2 = service.createGame().getId();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void makeMoveThrowsForNonExistentGame() {
        assertThatThrownBy(() -> service.makeMove("unknown", Player.X, new Position(0, 0)))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void makeMoveOnExistingGameUpdatesState() {
        var gameId = service.createGame().getId();
        service.makeMove(gameId, Player.X, new Position(0, 0));
        var game = service.makeMove(gameId, Player.O, new Position(1, 0));
        assertThat(game.getNextPlayer()).isEqualTo(Player.X);
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void getGameThrowsForUnknownId() {
        assertThatThrownBy(() -> service.getGame("unknown"))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    void getGameReturnsExistingGame() {
        var gameId = service.createGame().getId();
        var game = service.getGame(gameId);
        assertThat(game.getId()).isEqualTo(gameId);
    }

    @Test
    void getGameReturnsUpdatedStateAfterMove() {
        var gameId = service.createGame().getId();
        service.makeMove(gameId, Player.X, new Position(0, 0));

        var game = service.getGame(gameId);

        assertThat(game.getNextPlayer()).isEqualTo(Player.O);
        assertThat(game.getBoard().snapshot().getFirst().getFirst()).isEqualTo("X");
    }
}
