package com.flamingo.session.domain;

import com.flamingo.session.client.dto.PlayerValue;

import java.time.Instant;

public record Move(
        PlayerValue player,
        int row,
        int col,
        Instant timestamp
) {
}
