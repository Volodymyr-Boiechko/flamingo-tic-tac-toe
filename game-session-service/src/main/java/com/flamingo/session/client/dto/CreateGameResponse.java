package com.flamingo.session.client.dto;

import java.util.List;

public record CreateGameResponse(
        String gameId,
        List<List<String>> board,
        GameStatusValue status,
        PlayerValue nextPlayer
) {
}
