package com.flamingo.session.client.dto;

import java.util.List;

public record GameStateResponse(
        String gameId,
        List<List<String>> board,
        String status,
        String nextPlayer
) {
}
