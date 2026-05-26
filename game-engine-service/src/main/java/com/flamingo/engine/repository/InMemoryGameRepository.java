package com.flamingo.engine.repository;

import com.flamingo.engine.domain.Game;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory GameRepository backed by a ConcurrentHashMap. Suitable for
 * single-node deployments; state is lost on restart and not shared across
 * instances. For distributed setups, swap with a persistent implementation.
 */
@Repository
public class InMemoryGameRepository implements GameRepository {

    private final ConcurrentHashMap<String, Game> store = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        store.put(game.getId(), game);
    }

    @Override
    public Optional<Game> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
