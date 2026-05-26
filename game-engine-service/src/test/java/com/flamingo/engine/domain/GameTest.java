package com.flamingo.engine.domain;

import com.flamingo.engine.exception.GameAlreadyFinishedException;
import com.flamingo.engine.exception.InvalidMoveException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    @Test
    void newGameStartsWithXAndInProgress() {
        var game = new Game("g1");
        assertThat(game.getNextPlayer()).isEqualTo(Player.X);
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void afterXMovesTurnFlipsToO() {
        var game = new Game("g1");
        game.makeMove(new Position(0, 0), Player.X);
        assertThat(game.getNextPlayer()).isEqualTo(Player.O);
    }

    @Test
    void wrongPlayerThrowsInvalidMove() {
        var game = new Game("g1");
        assertThatThrownBy(() -> game.makeMove(new Position(0, 0), Player.O))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("expected X");
    }

    @Test
    void movingOnFinishedGameThrows() {
        var game = gameWhereXWonRow0();
        assertThatThrownBy(() -> game.makeMove(new Position(2, 0), Player.O))
                .isInstanceOf(GameAlreadyFinishedException.class);
    }

    @Test
    void statusUpdatesToXWonAfterWinningSequence() {
        var game = gameWhereXWonRow0();
        assertThat(game.getStatus()).isEqualTo(GameStatus.X_WON);
    }

    @Test
    void drawFinishesGameAndPreventsFurtherMoves() {
        var game = new Game("g1");
        game.makeMove(new Position(0, 0), Player.X);
        game.makeMove(new Position(0, 1), Player.O);
        game.makeMove(new Position(0, 2), Player.X);
        game.makeMove(new Position(1, 2), Player.O);
        game.makeMove(new Position(1, 0), Player.X);
        game.makeMove(new Position(2, 0), Player.O);
        game.makeMove(new Position(1, 1), Player.X);
        game.makeMove(new Position(2, 2), Player.O);
        game.makeMove(new Position(2, 1), Player.X);

        assertThat(game.getStatus()).isEqualTo(GameStatus.DRAW);
        assertThatThrownBy(() -> game.makeMove(new Position(2, 0), Player.O))
                .isInstanceOf(GameAlreadyFinishedException.class);
    }

    @Test
    void cannotMoveOnOccupiedCell() {
        var game = new Game("g1");
        game.makeMove(new Position(0, 0), Player.X);

        assertThatThrownBy(() -> game.makeMove(new Position(0, 0), Player.O))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("already occupied");
    }

    @Test
    void nextPlayerIsNullAfterWin() {
        var game = gameWhereXWonRow0();
        assertThat(game.getNextPlayer()).isNull();
    }

    @Test
    void nextPlayerIsNullAfterDraw() {
        var game = new Game("g1");
        game.makeMove(new Position(0, 0), Player.X);
        game.makeMove(new Position(0, 1), Player.O);
        game.makeMove(new Position(0, 2), Player.X);
        game.makeMove(new Position(1, 2), Player.O);
        game.makeMove(new Position(1, 0), Player.X);
        game.makeMove(new Position(2, 0), Player.O);
        game.makeMove(new Position(1, 1), Player.X);
        game.makeMove(new Position(2, 2), Player.O);
        game.makeMove(new Position(2, 1), Player.X);

        assertThat(game.getNextPlayer()).isNull();
    }

    private Game gameWhereXWonRow0() {
        var game = new Game("g1");
        game.makeMove(new Position(0, 0), Player.X);
        game.makeMove(new Position(1, 0), Player.O);
        game.makeMove(new Position(0, 1), Player.X);
        game.makeMove(new Position(1, 1), Player.O);
        game.makeMove(new Position(0, 2), Player.X);
        return game;
    }
}
