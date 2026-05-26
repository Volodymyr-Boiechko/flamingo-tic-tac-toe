package com.flamingo.engine.domain;

import com.flamingo.engine.exception.InvalidMoveException;

import java.util.Arrays;
import java.util.List;

/**
 * Encapsulates the 3×3 grid and all win/draw detection logic.
 */
public class Board {

    private static final int MAX_MOVES = 9;

    private final Player[][] grid = new Player[3][3];
    private int moveCount = 0;

    private static final Position[][] WINNING_LINES = {
            // rows
            {new Position(0, 0), new Position(0, 1), new Position(0, 2)},
            {new Position(1, 0), new Position(1, 1), new Position(1, 2)},
            {new Position(2, 0), new Position(2, 1), new Position(2, 2)},
            // columns
            {new Position(0, 0), new Position(1, 0), new Position(2, 0)},
            {new Position(0, 1), new Position(1, 1), new Position(2, 1)},
            {new Position(0, 2), new Position(1, 2), new Position(2, 2)},
            // diagonals
            {new Position(0, 0), new Position(1, 1), new Position(2, 2)},
            {new Position(0, 2), new Position(1, 1), new Position(2, 0)},
    };

    /**
     * Places a player's mark at the given position, validating vacancy.
     */
    public void placeMove(Position pos, Player player) {
        if (grid[pos.row()][pos.col()] != null) {
            throw new InvalidMoveException("cell already occupied");
        }
        grid[pos.row()][pos.col()] = player;
        moveCount++;
    }

    /**
     * Evaluates the current grid and returns the appropriate GameStatus.
     */
    public GameStatus checkStatus() {
        for (Position[] line : WINNING_LINES) {
            Player a = grid[line[0].row()][line[0].col()];
            Player b = grid[line[1].row()][line[1].col()];
            Player c = grid[line[2].row()][line[2].col()];
            if (a != null && a == b && a == c) {
                return a == Player.X ? GameStatus.X_WON : GameStatus.O_WON;
            }
        }
        return moveCount == MAX_MOVES ? GameStatus.DRAW : GameStatus.IN_PROGRESS;
    }

    /**
     * Returns an unmodifiable snapshot where each cell is "X", "O", or null.
     */
    public List<List<String>> snapshot() {
        return Arrays.stream(grid)
                .map(row -> Arrays.stream(row)
                        .map(cell -> cell == null ? null : cell.name())
                        .toList()
                )
                .toList();
    }
}
