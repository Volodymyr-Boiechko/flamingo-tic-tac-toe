package com.flamingo.session.client.dto;

public enum GameStatusValue {
    IN_PROGRESS,
    X_WON,
    O_WON,
    DRAW;

    public boolean isFinished() {
        return this != IN_PROGRESS;
    }
}
