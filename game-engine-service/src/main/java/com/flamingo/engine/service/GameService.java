package com.flamingo.engine.service;

import com.flamingo.engine.domain.Game;
import com.flamingo.engine.domain.Player;
import com.flamingo.engine.domain.Position;
import com.flamingo.engine.exception.GameNotFoundException;
import com.flamingo.engine.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application service that orchestrates game creation, retrieval and move execution.
 * <p>
 * Thread-safety: Game and Board mutations are NOT synchronized at the domain level.
 * In the Session Service simulation scenario, moves are issued sequentially per game,
 * so no concurrent mutation occurs. Should this service be exposed to concurrent
 * clients hitting the same gameId, per game synchronization should be added here
 * (e.g., ConcurrentHashMap of locks) to prevent lost updates and torn state.
 */
@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new game with a server-generated UUID.
     */
    public Game createGame() {
        var game = new Game(UUID.randomUUID().toString());
        repository.save(game);
        log.info("Created game {}", game.getId());
        return game;
    }

    /**
     * Applies a move to an existing game; throws GameNotFoundException if the game does not exist.
     */
    public Game makeMove(String gameId, Player player, Position position) {
        var game = getGame(gameId);
        game.makeMove(position, player);
        log.debug("Move recorded: game={} player={} position=({},{}) status={}",
                gameId, player, position.row(), position.col(), game.getStatus());
        if (game.getStatus().isFinished()) {
            log.info("Game {} finished with status {}", gameId, game.getStatus());
        }
        return game;
    }

    /**
     * Returns the game with the given id, or throws GameNotFoundException if absent.
     */
    public Game getGame(String gameId) {
        return repository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException("game not found: " + gameId));
    }
}
