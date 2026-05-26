package com.flamingo.session.repository;

import com.flamingo.session.domain.Session;

import java.util.Optional;

public interface SessionRepository {
    void save(Session session);

    Optional<Session> findById(String sessionId);
}
