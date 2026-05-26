package com.flamingo.engine.domain;

import com.flamingo.engine.exception.InvalidMoveException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionTest {

    @ParameterizedTest(name = "out of bounds ({0},{1})")
    @CsvSource({"-1, 0", "3, 0", "0, -1", "0, 3", "5, 5", "-1, -1"})
    void rejectsOutOfBoundsCoordinates(int row, int col) {
        assertThatThrownBy(() -> new Position(row, col))
                .isInstanceOf(InvalidMoveException.class)
                .hasMessageContaining("out of bounds");
    }

    @ParameterizedTest(name = "valid ({0},{1})")
    @CsvSource({"0,0", "0,1", "0,2", "1,0", "1,1", "1,2", "2,0", "2,1", "2,2"})
    void acceptsAllValidPositions(int row, int col) {
        assertThatNoException().isThrownBy(() -> new Position(row, col));
    }
}
