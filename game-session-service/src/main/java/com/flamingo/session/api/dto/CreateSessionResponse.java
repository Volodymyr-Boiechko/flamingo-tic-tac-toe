package com.flamingo.session.api.dto;

import java.time.Instant;

public record CreateSessionResponse(String sessionId, String gameId, Instant createdAt) {
}
