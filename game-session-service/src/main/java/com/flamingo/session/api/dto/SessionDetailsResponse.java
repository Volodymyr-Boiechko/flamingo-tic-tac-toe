package com.flamingo.session.api.dto;

import com.flamingo.session.domain.Session;
import com.flamingo.session.domain.SessionStatus;

import java.time.Instant;
import java.util.List;

public record SessionDetailsResponse(
        String sessionId,
        String gameId,
        SessionStatus status,
        List<MoveDto> moves,
        Instant createdAt,
        Instant finishedAt
) {
    public static SessionDetailsResponse from(Session session) {
        var moveDtos = session.getMoves().stream()
                .map(m -> new MoveDto(m.player(), m.row(), m.col(), m.timestamp()))
                .toList();
        return new SessionDetailsResponse(
                session.getSessionId(),
                session.getGameId(),
                session.getStatus(),
                moveDtos,
                session.getCreatedAt(),
                session.getFinishedAt()
        );
    }
}
