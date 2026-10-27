package com.algoviz.engine;

import java.util.function.LongSupplier;

/**
 * Accumulating stopwatch based on a monotonic clock ({@link System#nanoTime()}).
 * Time only accumulates while running, so paused time is excluded.
 * The clock is injectable so that tests can use a fake clock.
 */
public final class Stopwatch {

    private final LongSupplier clock;
    private long accumulated;
    private long startedAt;
    private boolean running;

    public Stopwatch() {
        this(System::nanoTime);
    }

    public Stopwatch(LongSupplier clock) {
        this.clock = clock;
    }

    public void start() {
        if (!running) {
            startedAt = clock.getAsLong();
            running = true;
        }
    }

    public void pause() {
        if (running) {
            accumulated += clock.getAsLong() - startedAt;
            running = false;
        }
    }

    public void reset() {
        accumulated = 0;
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public long elapsedNanos() {
        return running ? accumulated + (clock.getAsLong() - startedAt) : accumulated;
    }

    public double elapsedMillis() {
        return elapsedNanos() / 1_000_000.0;
    }
}
