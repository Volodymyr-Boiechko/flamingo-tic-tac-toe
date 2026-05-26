package com.flamingo.session.exception;

public class SessionAlreadyFinishedException extends RuntimeException {

    public SessionAlreadyFinishedException(String message) {
        super(message);
    }
}
