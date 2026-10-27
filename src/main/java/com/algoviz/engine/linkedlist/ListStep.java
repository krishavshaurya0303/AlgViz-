package com.algoviz.engine.linkedlist;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.StepStatus;

/** One recorded step of a linked-list operation. */
public final class ListStep implements ExecutionStep {

    private final ListOperation operation;
    private final ListSnapshot snapshot;
    private final ExecutionMetrics metrics;
    private final int line;
    private final String explanation;
    private final StepStatus status;

    ListStep(ListOperation operation, ListSnapshot snapshot, ExecutionMetrics metrics, int line,
             String explanation, StepStatus status) {
        this.operation = operation;
        this.snapshot = snapshot;
        this.metrics = metrics;
        this.line = line;
        this.explanation = explanation;
        this.status = status;
    }

    public ListOperation operation() {
        return operation;
    }

    public ListSnapshot snapshot() {
        return snapshot;
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
