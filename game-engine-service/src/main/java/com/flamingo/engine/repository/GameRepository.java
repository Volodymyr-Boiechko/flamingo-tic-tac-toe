package com.flamingo.engine.repository;

import com.flamingo.engine.domain.Game;

import java.util.Optional;

/**
 * Persistence abstraction for Game aggregates. Implementations may be
 * in-memory, JDBC-backed or distributed depending on deployment needs.
 */
public interface GameRepository {

    void save(Game game);

    Optional<Game> findById(String id);

}
