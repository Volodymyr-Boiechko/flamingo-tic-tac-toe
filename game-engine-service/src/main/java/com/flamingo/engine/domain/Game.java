package com.flamingo.engine.domain;

import com.flamingo.engine.exception.GameAlreadyFinishedException;
import com.flamingo.engine.exception.InvalidMoveException;

/**
 * Represents a single Tic Tac Toe game instance, including all state transitions.
 */
public class Game {

    private final String id;
    private final Board board;
    private Player nextPlayer;
    private GameStatus status;

    /**
     * Initialises a fresh game with the given id, X always moves first.
     */
    public Game(String id) {
        this.id = id;
        this.board = new Board();
        this.nextPlayer = Player.X;
        this.status = GameStatus.IN_PROGRESS;
    }

    /**
     * Applies a move on behalf of the given player, updating status and turn.
     */
    public void makeMove(Position pos, Player player) {
        if (status.isFinished()) {
            throw new GameAlreadyFinishedException("game is already finished with status: " + status);
        }
        if (player != nextPlayer) {
            throw new InvalidMoveException("not your turn, expected " + nextPlayer);
        }
        board.placeMove(pos, player);
        status = board.checkStatus();
        if (status.isFinished()) {
            nextPlayer = null;
        } else {
            nextPlayer = nextPlayer.opposite();
        }
    }

    public String getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public Player getNextPlayer() {
        return nextPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }
}
