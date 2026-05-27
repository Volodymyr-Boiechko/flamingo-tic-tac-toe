package com.flamingo.session.exception;

public class SessionAlreadyRunningException extends RuntimeException {

    public SessionAlreadyRunningException(String message) {
        super(message);
    }
}
