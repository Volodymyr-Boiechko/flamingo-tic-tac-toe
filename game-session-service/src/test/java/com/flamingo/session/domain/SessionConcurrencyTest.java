package com.flamingo.session.domain;

import com.flamingo.session.client.dto.PlayerValue;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

class SessionConcurrencyTest {

    @Test
    void getMovesIsThreadSafeWhileRecording() throws InterruptedException {
        var session = new Session("s1", "g1");
        int iterations = 1000;
        var latch = new CountDownLatch(2);
        var errors = new ConcurrentLinkedQueue<Throwable>();

        var writer = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    session.recordMove(new Move(
                            i % 2 == 0 ? PlayerValue.X : PlayerValue.O,
                            i % 3, i % 3,
                            Instant.now()));
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                latch.countDown();
            }
        });

        var reader = new Thread(() -> {
            try {
                for (int i = 0; i < iterations; i++) {
                    var snapshot = session.getMoves();
                    for (var m : snapshot) {
                        Objects.requireNonNull(m);
                    }
                }
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                latch.countDown();
            }
        });

        writer.start();
        reader.start();
        latch.await();

        assertThat(errors).isEmpty();
    }
}
