package com.flamingo.session.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoveGeneratorTest {

    @Test
    void nextMoveReturnsValidPositionOnEmptyBoard() {
        var generator = new MoveGenerator(new Random(42));
        var board = emptyBoard();

        var pos = generator.nextMove(board);

        assertThat(pos.row()).isBetween(0, 2);
        assertThat(pos.col()).isBetween(0, 2);
        assertThat(board.get(pos.row()).get(pos.col())).isNull();
    }

    @Test
    void nextMoveReturnsLastFreeCellWhenOnlyOneAvailable() {
        var generator = new MoveGenerator(new Random());
        var board = List.of(
                Arrays.asList("X", "O", "X"),
                Arrays.asList("O", null, "O"),
                Arrays.asList("X", "O", "X")
        );

        var pos = generator.nextMove(board);

        assertThat(pos.row()).isEqualTo(1);
        assertThat(pos.col()).isEqualTo(1);
    }

    @Test
    void nextMoveThrowsWhenBoardIsFull() {
        var generator = new MoveGenerator(new Random());
        var board = List.of(
                List.of("X", "O", "X"),
                List.of("O", "X", "O"),
                List.of("O", "X", "O")
        );

        assertThatThrownBy(() -> generator.nextMove(board))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("no free cells on board");
    }

    @Test
    void nextMoveAlwaysReturnsPreviouslyEmptyCell() {
        var board = Arrays.asList(
                Arrays.asList("X", null, "O"),
                Arrays.asList(null, "X", null),
                Arrays.asList("O", null, "X")
        );

        for (int i = 0; i < 20; i++) {
            var pos = new MoveGenerator(new Random()).nextMove(board);
            assertThat(pos.row()).isBetween(0, 2);
            assertThat(pos.col()).isBetween(0, 2);
            assertThat(board.get(pos.row()).get(pos.col())).isNull();
        }
    }

    private List<List<String>> emptyBoard() {
        var row = Arrays.asList((String) null, null, null);
        return List.of(row, row, row);
    }
}
