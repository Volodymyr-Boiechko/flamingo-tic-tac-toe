package com.flamingo.engine.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PositionDto(
        @NotNull @Min(0) @Max(2) Integer row,
        @NotNull @Min(0) @Max(2) Integer col
) {
}
