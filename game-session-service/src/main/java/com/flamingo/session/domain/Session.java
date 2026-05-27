package com.flamingo.session.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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
        this.moves = Collections.synchronizedList(new ArrayList<>());
        this.createdAt = Instant.now();
        this.finishedAt = null;
    }

    public void recordMove(Move move) {
        moves.add(move);
    }

    public synchronized boolean startSimulationIfNotStarted() {
        if (this.status != SessionStatus.CREATED) {
            return false;
        }
        this.status = SessionStatus.SIMULATING;
        return true;
    }

    public void markFinished() {
        this.status = SessionStatus.FINISHED;
        this.finishedAt = Instant.now();
    }

    public void markFailed() {
        this.status = SessionStatus.FAILED;
        this.finishedAt = Instant.now();
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
        synchronized (moves) {
            return List.copyOf(moves);
        }
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }
}
