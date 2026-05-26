package com.flamingo.session.client.dto;

import java.util.List;

public record GameStateResponse(
        String gameId,
        List<List<String>> board,
        GameStatusValue status,
        PlayerValue nextPlayer
) {
}
