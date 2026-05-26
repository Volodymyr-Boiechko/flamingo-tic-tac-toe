package com.flamingo.engine.domain;

import com.flamingo.engine.exception.InvalidMoveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ── Row wins ──────────────────────────────────────────────────────────────

    @ParameterizedTest(name = "row {0} win for {1}")
    @CsvSource({
            "0, X, X_WON",
            "1, X, X_WON",
            "2, X, X_WON",
            "0, O, O_WON",
            "1, O, O_WON",
            "2, O, O_WON",
    })
    void rowWin(int row, Player player, GameStatus expected) {
        for (int col = 0; col < 3; col++) {
            board.placeMove(new Position(row, col), player);
        }
        assertThat(board.checkStatus()).isEqualTo(expected);
    }

    // ── Column wins ───────────────────────────────────────────────────────────

    @ParameterizedTest(name = "column {0} win for {1}")
    @CsvSource({
            "0, X, X_WON",
            "1, X, X_WON",
            "2, X, X_WON",
            "0, O, O_WON",
            "1, O, O_WON",
            "2, O, O_WON",
    })
    void columnWin(int col, Player player, GameStatus expected) {
        for (int row = 0; row < 3; row++) {
            board.placeMove(new Position(row, col), player);
        }
        assertThat(board.checkStatus()).isEqualTo(expected);
    }

    // ── Diagonal wins ─────────────────────────────────────────────────────────

    @ParameterizedTest(name = "player {0} win")
    @CsvSource({
            "X, X_WON",
            "O, O_WON",
    })
    void mainDiagonalWin(Player player, GameStatus expected) {
        board.placeMove(new Position(0, 0), player);
        board.placeMove(new Position(1, 1), player);
        board.placeMove(new Position(2, 2), player);
        assertThat(board.checkStatus()).isEqualTo(expected);
    }

    @ParameterizedTest(name = "player {0} win")
    @CsvSource({
            "X, X_WON",
            "O, O_WON",
    })
    void antiDiagonalWin(Player player, GameStatus expected) {
        board.placeMove(new Position(0, 2), player);
        board.placeMove(new Position(1, 1), player);
        board.placeMove(new Position(2, 0), player);
        assertThat(board.checkStatus()).isEqualTo(expected);
    }

    // ── Draw ──────────────────────────────────────────────────────────────────

    @Test
    void draw() {
        // X O X
        // X X O
        // O X O
        board.placeMove(new Position(0, 0), Player.X);
        board.placeMove(new Position(0, 1), Player.O);
        board.placeMove(new Position(0, 2), Player.X);
        board.placeMove(new Position(1, 0), Player.X);
        board.placeMove(new Position(1, 1), Player.X);
        board.placeMove(new Position(1, 2), Player.O);
        board.placeMove(new Position(2, 0), Player.O);
        board.placeMove(new Position(2, 1), Player.X);
        board.placeMove(new Position(2, 2), Player.O);
        assertThat(board.checkStatus()).isEqualTo(GameStatus.DRAW);
    }

    // ── Cell occupied ─────────────────────────────────────────────────────────

    @Test
    void occupiedCellThrows() {
        board.placeMove(new Position(1, 1), Player.X);
        assertThatThrownBy(() -> board.placeMove(new Position(1, 1), Player.O))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("already occupied");
    }

    // ── In-progress ───────────────────────────────────────────────────────────

    @Test
    void inProgressAfterSingleMove() {
        board.placeMove(new Position(0, 0), Player.X);
        assertThat(board.checkStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void emptyBoardIsInProgress() {
        assertThat(board.checkStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    // ── Snapshot ──────────────────────────────────────────────────────────────

    @Test
    void snapshotReflectsPlacedMarks() {
        board.placeMove(new Position(0, 0), Player.X);
        board.placeMove(new Position(1, 1), Player.O);
        assertThat(board.snapshot())
                .containsExactly(
                        Arrays.asList("X", null, null),
                        Arrays.asList(null, "O", null),
                        Arrays.asList(null, null, null));
    }

    @Test
    void snapshotPreservesNullsForEmptyCells() {
        board.placeMove(new Position(0, 0), Player.X);
        var snapshot = board.snapshot();
        assertThat(snapshot.get(0)).containsExactly("X", null, null);
        assertThat(snapshot.get(1)).containsExactly(null, null, null);
        assertThat(snapshot.get(2)).containsExactly(null, null, null);
    }
}
