package com.flamingo.engine.exception;

/**
 * Thrown when a move is attempted on a game that has already finished.
 */
public class GameAlreadyFinishedException extends RuntimeException {

    public GameAlreadyFinishedException(String message) {
        super(message);
    }
}
