package com.flamingo.session.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Session {

    private final String sessionId;
    private final String gameId;
    private SessionStatus status;
    private final List<Move> moves;
    private final Instant createdAt;
    private Instant finishedAt;

    public Session(String sessionId, String gameId) {
        this.sessionId = sessionId;
        this.gameId = gameId;
        this.status = SessionStatus.CREATED;
        this.moves = new ArrayList<>();
        this.createdAt = Instant.now();
        this.finishedAt = null;
    }

    public void recordMove(Move move) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void markFinished() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getGameId() {
        return gameId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
