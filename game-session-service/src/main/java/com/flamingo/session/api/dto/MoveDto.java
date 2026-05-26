package com.flamingo.session.api.dto;

import com.flamingo.session.client.dto.PlayerValue;

import java.time.Instant;

public record MoveDto(
        PlayerValue player,
        int row,
        int col,
        Instant timestamp
) {
}
