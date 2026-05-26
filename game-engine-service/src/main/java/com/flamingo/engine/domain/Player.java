package com.flamingo.engine.domain;

/**
 * Represents one of the two players in a Tic Tac Toe game.
 */
public enum Player {
    X, O;

    public Player opposite() {
        return this == X ? O : X;
    }
}
