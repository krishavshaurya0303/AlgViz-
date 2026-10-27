package com.algoviz.engine;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * UI-independent playback state machine shared by all four modules.
 * <p>
 * It owns the {@link ExecutionHistory} cursor and the playback {@link Stopwatch}
 * and notifies {@link PlaybackListener}s (Observer pattern) after every change.
 * Automatic playback is driven from outside by calling {@link #tick()} — in the
 * application a JavaFX Timeline does this on the FX thread; in tests the test
 * calls it directly. Because the controller holds no timer itself it is fully
 * deterministic and testable without JavaFX.
 * <p>
 * Every {@link #load(ExecutionTrace)} or {@link #clear()} increments a
 * generation number. Callers that schedule work asynchronously capture the
 * generation and discard the result if it has changed (stale-update guard).
 */
public final class PlaybackController<S extends ExecutionStep> {

    private final List<PlaybackListener<S>> listeners = new CopyOnWriteArrayList<>();
    private final Stopwatch stopwatch;
    private ExecutionHistory<S> history;
    private PlaybackState state = PlaybackState.EMPTY;
    private long generation;

    public PlaybackController() {
        this(new Stopwatch());
    }

    public PlaybackController(Stopwatch stopwatch) {
        this.stopwatch = Objects.requireNonNull(stopwatch);
    }

    // ------------------------------------------------------------------ setup

    /** Loads a new trace, positioned at its initial state. */
    public void load(ExecutionTrace<S> trace) {
        Objects.requireNonNull(trace);
        generation++;
        history = new ExecutionHistory<>(trace);
        stopwatch.reset();
        state = trace.size() == 1 ? PlaybackState.FINISHED : PlaybackState.READY;
        fire();
    }

    /** Loads a trace and immediately jumps to its final step (used for "instant" mode). */
    public void loadAtEnd(ExecutionTrace<S> trace) {
        load(trace);
        history.seekEnd();
        state = PlaybackState.FINISHED;
        fire();
    }

    /** Removes the trace entirely. */
    public void clear() {
        generation++;
        history = null;
        stopwatch.reset();
        state = PlaybackState.EMPTY;
        fire();
    }

    // --------------------------------------------------------------- commands

    /** Starts or resumes automatic playback. Playing from the end replays from the start. */
    public void play() {
        if (history == null || state == PlaybackState.PLAYING) {
            return;
        }
        if (history.isAtEnd()) {
            if (history.size() == 1) {
                return;
            }
            history.rewind();
            stopwatch.reset();
        }
        state = PlaybackState.PLAYING;
        stopwatch.start();
        fire();
    }

    public void pause() {
        if (state != PlaybackState.PLAYING) {
            return;
        }
        stopwatch.pause();
        state = PlaybackState.PAUSED;
        fire();
    }

    public void togglePlayPause() {
        if (state == PlaybackState.PLAYING) {
            pause();
        } else {
            play();
        }
    }

    /**
     * Advances one step if playing. Called by the timer.
     *
     * @return true if playback is still running afterwards
     */
    public boolean tick() {
        if (state != PlaybackState.PLAYING || history == null) {
            return false;
        }
        history.stepForward();
        if (history.isAtEnd()) {
            stopwatch.pause();
            state = PlaybackState.FINISHED;
        }
        fire();
        return state == PlaybackState.PLAYING;
    }

    /** Manual single step forward. Stops automatic playback first. */
    public boolean stepForward() {
        if (history == null) {
            return false;
        }
        stopwatch.pause();
        boolean moved = history.stepForward();
        state = history.isAtEnd() ? PlaybackState.FINISHED : PlaybackState.PAUSED;
        fire();
        return moved;
    }

    /** Manual single step backward — restores the previously recorded snapshot. */
    public boolean stepBackward() {
        if (history == null) {
            return false;
        }
        stopwatch.pause();
        boolean moved = history.stepBackward();
        state = history.index() == 0 ? PlaybackState.READY : PlaybackState.PAUSED;
        fire();
        return moved;
    }

    /** Returns to the initial state and clears the elapsed time. */
    public void reset() {
        if (history == null) {
            return;
        }
        history.rewind();
        stopwatch.reset();
        state = history.size() == 1 ? PlaybackState.FINISHED : PlaybackState.READY;
        fire();
    }

    /** Jumps directly to the final step. */
    public void skipToEnd() {
        if (history == null) {
            return;
        }
        stopwatch.pause();
        history.seekEnd();
        state = PlaybackState.FINISHED;
        fire();
    }

    /** Jumps to an arbitrary step (used by the progress slider). */
    public void seek(int index) {
        if (history == null) {
            return;
        }
        stopwatch.pause();
        history.seek(index);
        if (history.isAtEnd()) {
            state = PlaybackState.FINISHED;
        } else {
            state = index == 0 ? PlaybackState.READY : PlaybackState.PAUSED;
        }
        fire();
    }

    // ---------------------------------------------------------------- queries

    public PlaybackState state() {
        return state;
    }

    public boolean hasTrace() {
        return history != null;
    }

    public boolean isPlaying() {
        return state == PlaybackState.PLAYING;
    }

    public S currentStep() {
        return history == null ? null : history.current();
    }

    public int index() {
        return history == null ? 0 : history.index();
    }

    public int size() {
        return history == null ? 0 : history.size();
    }

    public boolean canStepForward() {
        return history != null && history.canStepForward();
    }

    public boolean canStepBackward() {
        return history != null && history.canStepBackward();
    }

    public ExecutionTrace<S> trace() {
        return history == null ? null : history.trace();
    }

    public long generation() {
        return generation;
    }

    public long elapsedNanos() {
        return stopwatch.elapsedNanos();
    }

    // -------------------------------------------------------------- observers

    public void addListener(PlaybackListener<S> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(PlaybackListener<S> listener) {
        listeners.remove(listener);
    }

    private void fire() {
        S step = currentStep();
        int idx = index();
        for (PlaybackListener<S> l : listeners) {
            l.onPlaybackChanged(step, idx, state);
        }
    }
}
