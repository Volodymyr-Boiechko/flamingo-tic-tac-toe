package com.flamingo.engine.exception;

/**
 * Thrown when a move violates game rules (out of bounds, occupied cell, wrong turn).
 */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
