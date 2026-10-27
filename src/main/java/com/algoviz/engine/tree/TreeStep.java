package com.algoviz.engine.tree;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.StepStatus;

import java.util.List;
import java.util.Map;

/** Immutable snapshot of the tree and its highlights at one step of an operation. */
public final class TreeStep implements ExecutionStep {

    private final TreeOperation operation;
    private final TreeSnapshot tree;
    private final Map<Integer, TreeHighlight> highlights;
    private final List<Integer> output;
    private final ExecutionMetrics metrics;
    private final int line;
    private final String explanation;
    private final StepStatus status;

    TreeStep(TreeOperation operation, TreeSnapshot tree, Map<Integer, TreeHighlight> highlights,
             List<Integer> output, ExecutionMetrics metrics, int line, String explanation, StepStatus status) {
        this.operation = operation;
        this.tree = tree;
        this.highlights = Map.copyOf(highlights);
        this.output = List.copyOf(output);
        this.metrics = metrics;
        this.line = line;
        this.explanation = explanation;
        this.status = status;
    }

    public TreeOperation operation() {
        return operation;
    }

    public TreeSnapshot tree() {
        return tree;
    }

    /** Highlight of a node id, or null if the node is not highlighted. */
    public TreeHighlight highlightOf(int nodeId) {
        return highlights.get(nodeId);
    }

    public Map<Integer, TreeHighlight> highlights() {
        return highlights;
    }

    /** Values output so far by a traversal (empty for other operations). */
    public List<Integer> output() {
        return output;
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
