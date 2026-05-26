package com.flamingo.engine.exception;

/**
 * Thrown when a requested game id does not exist in the repository.
 */
public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String message) {
        super(message);
    }
}
