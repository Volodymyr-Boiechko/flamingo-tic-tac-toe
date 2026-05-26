package com.flamingo.session.api.dto;

import com.flamingo.session.domain.Session;

import java.time.Instant;

public record CreateSessionResponse(
        String sessionId,
        String gameId,
        Instant createdAt
) {
    public static CreateSessionResponse from(Session session) {
        return new CreateSessionResponse(
                session.getSessionId(),
                session.getGameId(),
                session.getCreatedAt()
        );
    }
}
