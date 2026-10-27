package com.algoviz.engine;

/**
 * Observer notified whenever the playback position or state changes.
 * The UI registers one of these to re-render; the engine never knows about JavaFX.
 */
@FunctionalInterface
public interface PlaybackListener<S extends ExecutionStep> {

    /**
     * @param step  the step now displayed (null when no trace is loaded)
     * @param index position of the step in the trace
     * @param state current playback state
     */
    void onPlaybackChanged(S step, int index, PlaybackState state);
}
