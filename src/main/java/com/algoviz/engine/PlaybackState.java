package com.algoviz.engine;

/** Lifecycle of a playback session. */
public enum PlaybackState {
    /** No trace loaded. */
    EMPTY("Idle"),
    /** Trace loaded, positioned at the initial state. */
    READY("Ready"),
    /** Advancing automatically on every tick. */
    PLAYING("Playing"),
    /** Stopped part-way; can resume or step. */
    PAUSED("Paused"),
    /** Positioned at the final step. */
    FINISHED("Finished");

    private final String label;

    PlaybackState(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
