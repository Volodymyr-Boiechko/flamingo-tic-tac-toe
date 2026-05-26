package com.flamingo.session.domain;

import java.time.Instant;

public record Move(
        String player,
        int row,
        int col,
        Instant timestamp
) {
}
