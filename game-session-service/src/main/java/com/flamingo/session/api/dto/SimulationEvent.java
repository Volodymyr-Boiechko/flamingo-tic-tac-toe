package com.flamingo.session.api.dto;

import com.flamingo.session.client.dto.GameStatusValue;

public record SimulationEvent(
        SimulationEventType type,
        MoveDto move,
        GameStatusValue status,
        String message
) {
}
