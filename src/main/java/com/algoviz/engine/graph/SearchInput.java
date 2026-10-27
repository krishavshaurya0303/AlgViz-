package com.algoviz.engine.graph;

import java.util.Objects;

/** A graph plus start and target nodes. */
public record SearchInput(Graph graph, int start, int target) {

    public SearchInput {
        Objects.requireNonNull(graph, "graph");
        if (start < 0 || start >= graph.nodeCount() || target < 0 || target >= graph.nodeCount()) {
            throw new IllegalArgumentException("Start and target must be nodes of the graph");
        }
        if (graph.isBlocked(start) || graph.isBlocked(target)) {
            throw new IllegalArgumentException("Start and target cannot be walls");
        }
    }
}
