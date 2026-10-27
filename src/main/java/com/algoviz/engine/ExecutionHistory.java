package com.algoviz.engine;

import java.util.Objects;

/**
 * A cursor over an {@link ExecutionTrace}. Moving the cursor forward or backward
 * selects a previously recorded snapshot; nothing is recomputed, so moving
 * back and forth any number of times always yields identical states.
 */
public final class ExecutionHistory<S extends ExecutionStep> {

    private final ExecutionTrace<S> trace;
    private int cursor;

    public ExecutionHistory(ExecutionTrace<S> trace) {
        this.trace = Objects.requireNonNull(trace);
        this.cursor = 0;
    }

    public ExecutionTrace<S> trace() {
        return trace;
    }

    public S current() {
        return trace.get(cursor);
    }

    public int index() {
        return cursor;
    }

    public int size() {
        return trace.size();
    }

    public boolean canStepForward() {
        return cursor < trace.size() - 1;
    }

    public boolean canStepBackward() {
        return cursor > 0;
    }

    public boolean isAtEnd() {
        return !canStepForward();
    }

    public boolean stepForward() {
        if (!canStepForward()) {
            return false;
        }
        cursor++;
        return true;
    }

    public boolean stepBackward() {
        if (!canStepBackward()) {
            return false;
        }
        cursor--;
        return true;
    }

    public void rewind() {
        cursor = 0;
    }

    public void seek(int index) {
        if (index < 0 || index >= trace.size()) {
            throw new IndexOutOfBoundsException("Step " + index + " outside 0.." + (trace.size() - 1));
        }
        cursor = index;
    }

    public void seekEnd() {
        cursor = trace.size() - 1;
    }
}
