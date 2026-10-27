package com.algoviz.engine.graph;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.StepStatus;

import java.util.List;

/** Immutable snapshot of every node's search state at one step. */
public final class GraphStep implements ExecutionStep {

    private static final NodeVisual[] VISUALS = NodeVisual.values();

    private final byte[] states;
    private final int current;
    private final int[] distances;
    private final List<Integer> path;
    private final SearchOutcome outcome;
    private final ExecutionMetrics metrics;
    private final int line;
    private final String explanation;
    private final StepStatus status;

    GraphStep(byte[] states, int current, int[] distances, List<Integer> path, SearchOutcome outcome,
              ExecutionMetrics metrics, int line, String explanation, StepStatus status) {
        this.states = states.clone();
        this.current = current;
        this.distances = distances == null ? null : distances.clone();
        this.path = List.copyOf(path);
        this.outcome = outcome;
        this.metrics = metrics;
        this.line = line;
        this.explanation = explanation;
        this.status = status;
    }

    public NodeVisual stateOf(int node) {
        return VISUALS[states[node]];
    }

    public int nodeCount() {
        return states.length;
    }

    /** Node expanded in this step, or -1. */
    public int current() {
        return current;
    }

    public boolean hasDistances() {
        return distances != null;
    }

    /** Best known distance, or {@link Integer#MAX_VALUE} for "infinity". */
    public int distance(int node) {
        return distances[node];
    }

    /** Path traced so far (complete once the outcome is FOUND and tracing finished). */
    public List<Integer> path() {
        return path;
    }

    public SearchOutcome outcome() {
        return outcome;
    }

    @Override
    public ExecutionMetrics metrics() {
        return metrics;
    }

    @Override
    public int pseudocodeLine() {
        return line;
    }

    @Override
    public String explanation() {
        return explanation;
    }

    @Override
    public StepStatus status() {
        return status;
    }
}
