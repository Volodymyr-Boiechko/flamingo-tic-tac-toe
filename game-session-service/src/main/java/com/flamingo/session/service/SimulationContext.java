package com.flamingo.session.service;

import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.client.dto.GameStateResponse;
import reactor.core.publisher.Sinks;

public class SimulationContext {

    private final String sessionId;
    private final Sinks.Many<SimulationEvent> events;
    private GameStateResponse currentState;

    public SimulationContext(String sessionId) {
        this.sessionId = sessionId;
        this.events = Sinks.many().replay().all();
    }

    public String sessionId() {
        return sessionId;
    }

    public Sinks.Many<SimulationEvent> events() {
        return events;
    }

    public GameStateResponse currentState() {
        return currentState;
    }

    public void updateState(GameStateResponse state) {
        this.currentState = state;
    }
}
