package com.flamingo.session.service;

import com.flamingo.session.client.dto.Position;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class MoveGenerator {

    private final Random random;

    public MoveGenerator() {
        this(new Random());
    }

    public MoveGenerator(Random random) {
        this.random = random;
    }

    public Position nextMove(List<List<String>> board) {
        var freeCells = new ArrayList<Position>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board.get(r).get(c) == null) {
                    freeCells.add(new Position(r, c));
                }
            }
        }
        if (freeCells.isEmpty()) {
            throw new IllegalStateException("no free cells on board");
        }
        return freeCells.get(random.nextInt(freeCells.size()));
    }
}
