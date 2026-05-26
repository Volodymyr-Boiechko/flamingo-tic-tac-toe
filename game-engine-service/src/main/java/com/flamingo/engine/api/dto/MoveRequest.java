package com.flamingo.engine.api.dto;

import com.flamingo.engine.domain.Player;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(
        @NotNull Player player,
        @NotNull @Valid PositionDto position
) {
}
