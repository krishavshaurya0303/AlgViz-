package com.algoviz.engine;

import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.StepStatus;

/**
 * One logical, self-contained state of an algorithm's execution.
 * <p>
 * Implementations are immutable snapshots: they contain everything needed to
 * render the visualization (data, highlights, counters, pseudocode line and an
 * explanation). The UI never re-runs the algorithm to draw a step — it simply
 * renders the snapshot. This is what makes Step Backward exact.
 */
public interface ExecutionStep {

    /** Beginner-friendly description of what happened in this step. */
    String explanation();

    /** Zero-based index into the algorithm's pseudocode, or -1 if no line applies. */
    int pseudocodeLine();

    /** Counter values after this step. */
    ExecutionMetrics metrics();

    /** Tone of the step (used for colouring the explanation). */
    default StepStatus status() {
        return StepStatus.INFO;
    }
}
