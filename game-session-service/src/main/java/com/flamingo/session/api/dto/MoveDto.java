package com.flamingo.session.api.dto;

import java.time.Instant;

public record MoveDto(
        String player,
        int row,
        int col,
        Instant timestamp
) {
}
