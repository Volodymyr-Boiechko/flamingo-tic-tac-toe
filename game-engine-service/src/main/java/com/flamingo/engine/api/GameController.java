package com.flamingo.engine.api;

import com.flamingo.engine.api.dto.GameStateResponse;
import com.flamingo.engine.api.dto.MoveRequest;
import com.flamingo.engine.domain.Position;
import com.flamingo.engine.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for game lifecycle (creation, moves, state retrieval).
 */
@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GameStateResponse createGame() {
        var game = gameService.createGame();
        return GameStateResponse.from(game);
    }

    @PostMapping("/{gameId}/move")
    public GameStateResponse makeMove(@PathVariable String gameId,
                                      @Valid @RequestBody MoveRequest req) {
        var position = new Position(req.position().row(), req.position().col());
        var game = gameService.makeMove(gameId, req.player(), position);
        return GameStateResponse.from(game);
    }

    @GetMapping("/{gameId}")
    public GameStateResponse getGame(@PathVariable String gameId) {
        return GameStateResponse.from(gameService.getGame(gameId));
    }
}
