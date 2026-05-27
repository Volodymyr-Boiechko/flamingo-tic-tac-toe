package com.flamingo.session.service;

import com.flamingo.session.api.dto.SimulationEvent;
import com.flamingo.session.client.dto.GameStateResponse;
import reactor.core.publisher.Sinks;

/**
 * Mutable holder for a single simulation's runtime state: the event sink fed by
 * {@link GameSimulator} and the latest {@link GameStateResponse} returned by the
 * engine.
 *
 * <p>Not thread-safe by design — accessed only from the single subscriber thread
 * of the simulation pipeline.
 */
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
