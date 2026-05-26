package com.flamingo.engine.api;

import com.flamingo.engine.domain.Game;
import com.flamingo.engine.domain.Player;
import com.flamingo.engine.domain.Position;
import com.flamingo.engine.exception.GameAlreadyFinishedException;
import com.flamingo.engine.exception.GameNotFoundException;
import com.flamingo.engine.exception.InvalidMoveException;
import com.flamingo.engine.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Test
    void postGamesReturns201WithInitialState() throws Exception {
        var gameId = UUID.randomUUID().toString();
        when(gameService.createGame()).thenReturn(new Game(gameId));

        mockMvc.perform(post("/games"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.nextPlayer").value("X"));
    }

    @Test
    void postValidMoveReturns200WithGameState() throws Exception {
        var game = new Game("game-1");
        game.makeMove(new Position(0, 0), Player.X);
        when(gameService.makeMove(eq("game-1"), eq(Player.X), any(Position.class))).thenReturn(game);

        mockMvc.perform(post("/games/game-1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-1"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.nextPlayer").value("O"))
                .andExpect(jsonPath("$.board[0][0]").value("X"))
                .andExpect(jsonPath("$.board[1][1]").doesNotExist());
    }

    @Test
    void postMoveOnNonExistentGameReturns404() throws Exception {
        when(gameService.makeMove(eq("missing"), any(), any()))
                .thenThrow(new GameNotFoundException("game not found: missing"));

        mockMvc.perform(post("/games/missing/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Game Not Found"));
    }

    @Test
    void postNullPlayerReturns400WithProblemDetail() throws Exception {
        mockMvc.perform(post("/games/game-1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":null,"position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.player").exists());
    }

    @Test
    void postOutOfBoundsPositionReturns400() throws Exception {
        mockMvc.perform(post("/games/game-1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":5,"col":0}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    void postOnFinishedGameReturns409() throws Exception {
        when(gameService.makeMove(eq("game-1"), any(), any()))
                .thenThrow(new GameAlreadyFinishedException("game is already finished with status: X_WON"));

        mockMvc.perform(post("/games/game-1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Game Already Finished"));
    }

    @Test
    void getExistingGameReturns200() throws Exception {
        when(gameService.getGame("game-1")).thenReturn(new Game("game-1"));

        mockMvc.perform(get("/games/game-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-1"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getUnknownGameReturns404WithProblemDetail() throws Exception {
        when(gameService.getGame("unknown"))
                .thenThrow(new GameNotFoundException("game not found: unknown"));

        mockMvc.perform(get("/games/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Game Not Found"))
                .andExpect(jsonPath("$.detail").value("game not found: unknown"));
    }

    @Test
    void postOnOccupiedCellReturns400() throws Exception {
        when(gameService.makeMove(eq("game-1"), any(), any()))
                .thenThrow(new InvalidMoveException("cell already occupied"));

        mockMvc.perform(post("/games/game-1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Move"))
                .andExpect(jsonPath("$.detail").value("cell already occupied"));
    }

    @Test
    void postWrongTurnReturns400() throws Exception {
        when(gameService.makeMove(eq("game-1"), any(), any()))
                .thenThrow(new InvalidMoveException("not your turn, expected X"));

        mockMvc.perform(post("/games/game-1/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"O","position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("not your turn, expected X"));
    }
}
