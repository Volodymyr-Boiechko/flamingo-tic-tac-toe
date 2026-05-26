package com.flamingo.session.service;

import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.api.dto.SimulationEventType;
import com.flamingo.session.client.dto.GameStateResponse;
import com.flamingo.session.client.dto.GameStatusValue;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SimulationContextTest {

    @Test
    void newContextHasNoState() {
        var context = new SimulationContext("session-1");

        assertThat(context.currentState()).isNull();
        assertThat(context.events()).isNotNull();
    }

    @Test
    void updateStateStoresLatestState() {
        var context = new SimulationContext("session-1");
        var state = new GameStateResponse("game-1", List.of(), GameStatusValue.IN_PROGRESS, null);

        context.updateState(state);

        assertThat(context.currentState()).isSameAs(state);
    }

    @Test
    void eventsSinkAcceptsEmissionsBeforeSimulationStarts() {
        var context = new SimulationContext("session-1");
        var event = new SimulationEvent(SimulationEventType.MOVE_MADE, null, null, "test");

        context.events().tryEmitNext(event);

        StepVerifier.create(context.events().asFlux().take(1))
                .expectNext(event)
                .verifyComplete();
    }
}
