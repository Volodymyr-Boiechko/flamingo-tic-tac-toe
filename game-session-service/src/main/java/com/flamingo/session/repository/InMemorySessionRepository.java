package com.flamingo.session.repository;

import com.flamingo.session.domain.Session;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemorySessionRepository implements SessionRepository {

    private final ConcurrentHashMap<String, Session> store = new ConcurrentHashMap<>();

    @Override
    public void save(Session session) {
        store.put(session.getSessionId(), session);
    }

    @Override
    public Optional<Session> findById(String sessionId) {
        return Optional.ofNullable(store.get(sessionId));
    }
}
