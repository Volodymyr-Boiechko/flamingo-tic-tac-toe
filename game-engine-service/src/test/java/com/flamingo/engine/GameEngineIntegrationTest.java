package com.flamingo.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flamingo.engine.domain.GameStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameEngineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Creates a game via POST /games, then plays a complete X-wins sequence through the
     * REST API and asserts the final state. Move sequence: X(0,0) O(1,0) X(0,1) O(1,1) X(0,2).
     */
    @Test
    @DisplayName("Full game flow: create → 5 moves → X wins by top row")
    void fullXWinsGameFlow() throws Exception {
        var gameId = createGame();

        playMove(gameId, "X", 0, 0);
        playMove(gameId, "O", 1, 0);
        playMove(gameId, "X", 0, 1);
        playMove(gameId, "O", 1, 1);

        // Final move — X completes the top row
        mockMvc.perform(post("/games/{id}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":0,"col":2}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(GameStatus.X_WON.name()))
                .andExpect(jsonPath("$.nextPlayer").doesNotExist());

        // Confirm
        mockMvc.perform(get("/games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(GameStatus.X_WON.name()))
                .andExpect(jsonPath("$.board[0][0]").value("X"))
                .andExpect(jsonPath("$.board[0][1]").value("X"))
                .andExpect(jsonPath("$.board[0][2]").value("X"));
    }

    @Test
    @DisplayName("Full game flow: create → 9 moves → draw")
    void fullDrawGameFlow() throws Exception {
        var gameId = createGame();

        // Play sequence that results in a draw:
        // X O X
        // X X O
        // O X O
        playMove(gameId, "X", 0, 0);
        playMove(gameId, "O", 0, 1);
        playMove(gameId, "X", 0, 2);
        playMove(gameId, "O", 1, 2);
        playMove(gameId, "X", 1, 0);
        playMove(gameId, "O", 2, 0);
        playMove(gameId, "X", 1, 1);
        playMove(gameId, "O", 2, 2);

        // Finish the board without a winner
        mockMvc.perform(post("/games/{id}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"X","position":{"row":2,"col":1}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(GameStatus.DRAW.name()))
                .andExpect(jsonPath("$.nextPlayer").doesNotExist());

        // Confirm draw
        mockMvc.perform(get("/games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(GameStatus.DRAW.name()))
                .andExpect(jsonPath("$.board[0][0]").value("X"))
                .andExpect(jsonPath("$.board[1][1]").value("X"))
                .andExpect(jsonPath("$.board[2][2]").value("O"));

        // Try to make another move results in error
        mockMvc.perform(post("/games/{id}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"O","position":{"row":0,"col":0}}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Game Already Finished"));
    }

    private String createGame() throws Exception {
        var createResult = mockMvc.perform(post("/games"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("gameId").asText();
    }

    private void playMove(String gameId, String player, int row, int col) throws Exception {
        mockMvc.perform(post("/games/{id}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player":"%s","position":{"row":%d,"col":%d}}
                                """.formatted(player, row, col)))
                .andExpect(status().isOk());
    }
}
