package com.flamingo.session.api.dto;

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
}
