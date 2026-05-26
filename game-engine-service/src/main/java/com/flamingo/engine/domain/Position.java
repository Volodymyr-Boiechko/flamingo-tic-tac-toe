package com.flamingo.engine.domain;

import com.flamingo.engine.exception.InvalidMoveException;

public record Position(int row, int col) {

    public Position {
        if (row < 0 || row > 2 || col < 0 || col > 2) {
            throw new InvalidMoveException(
                    "position out of bounds: (%d,%d)".formatted(row, col)
            );
        }
    }
}
