package com.flamingo.session.api.dto;

public record SimulationEvent(
        String type,
        MoveDto move,
        String status,
        String message
) {
}
