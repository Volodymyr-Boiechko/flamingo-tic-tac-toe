package com.flamingo.session.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SimulationContextRegistryTest {

    private final SimulationContextRegistry registry = new SimulationContextRegistry();

    @Test
    void getOrCreateReturnsNewContextWhenAbsent() {
        var context = registry.getOrCreate("session-1");

        assertThat(context.sessionId()).isEqualTo("session-1");
        assertThat(context.events()).isNotNull();
        assertThat(context.currentState()).isNull();
    }

    @Test
    void getOrCreateReturnsSameInstanceOnRepeatedCalls() {
        var first = registry.getOrCreate("session-1");
        var second = registry.getOrCreate("session-1");

        assertThat(second).isSameAs(first);
    }

    @Test
    void findReturnsEmptyWhenAbsent() {
        assertThat(registry.find("unknown")).isEmpty();
    }

    @Test
    void findReturnsContextWhenPresent() {
        var created = registry.getOrCreate("session-1");

        assertThat(registry.find("session-1")).contains(created);
    }

    @Test
    void removeRemovesContext() {
        registry.getOrCreate("session-1");
        registry.remove("session-1");

        assertThat(registry.find("session-1")).isEmpty();
    }
}
