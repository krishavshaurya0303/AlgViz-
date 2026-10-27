package com.algoviz.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable, ordered sequence of execution steps produced by one algorithm run.
 * The first step is always the initial state (before any operation).
 *
 * @param <S> the step type of the module
 */
public final class ExecutionTrace<S extends ExecutionStep> {

    private final String title;
    private final List<S> steps;
    private final long buildNanos;

    public ExecutionTrace(String title, List<S> steps, long buildNanos) {
        Objects.requireNonNull(steps, "steps");
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("A trace needs at least one step (the initial state)");
        }
        this.title = Objects.requireNonNull(title, "title");
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
        this.buildNanos = buildNanos;
    }

    public String title() {
        return title;
    }

    public int size() {
        return steps.size();
    }

    public S get(int index) {
        return steps.get(index);
    }

    public S first() {
        return steps.get(0);
    }

    public S last() {
        return steps.get(steps.size() - 1);
    }

    public List<S> steps() {
        return steps;
    }

    /**
     * Wall-clock time used to run the instrumented algorithm and record all
     * snapshots. This includes recording overhead, so it is not a benchmark.
     */
    public long buildNanos() {
        return buildNanos;
    }
}
