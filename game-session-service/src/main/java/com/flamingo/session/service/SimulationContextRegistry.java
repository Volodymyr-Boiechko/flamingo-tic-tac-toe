package com.flamingo.session.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-session holder for runtime simulation state (event sink + current game snapshot).
 * Keyed by {@code sessionId}.
 *
 * <p>Entries are added by {@link GameSimulator#simulate} or
 * {@link GameSimulator#eventStream} (whichever runs first) and removed when
 * the simulation completes or fails. The {@link reactor.core.publisher.Sinks.Many}
 * held in each {@link SimulationContext} is a {@code replay().all()} sink, so
 * late SSE subscribers still receive the full event sequence as long as they
 * retain a reference to the {@link reactor.core.publisher.Flux} obtained before
 * removal.
 */
@Component
public class SimulationContextRegistry {

    private final ConcurrentHashMap<String, SimulationContext> contexts = new ConcurrentHashMap<>();

    public SimulationContext getOrCreate(String sessionId) {
        return contexts.computeIfAbsent(sessionId, k -> new SimulationContext(sessionId));
    }

    public Optional<SimulationContext> find(String sessionId) {
        return Optional.ofNullable(contexts.get(sessionId));
    }

    public void remove(String sessionId) {
        contexts.remove(sessionId);
    }
}
