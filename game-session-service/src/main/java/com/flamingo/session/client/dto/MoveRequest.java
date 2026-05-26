package com.flamingo.session.client.dto;

public record MoveRequest(
        PlayerValue player,
        Position position
) {
}
