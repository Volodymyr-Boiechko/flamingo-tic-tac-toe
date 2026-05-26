package com.flamingo.session.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
