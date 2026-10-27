package com.algoviz.engine.graph;

/**
 * Abstraction over any graph the search algorithms can run on.
 * Nodes are identified by integers 0..nodeCount()-1. The grid in the Graph
 * Explorer is one implementation; tests also use a general weighted graph.
 */
public interface Graph {

    int nodeCount();

    /** Blocked nodes (walls) are never entered. */
    boolean isBlocked(int node);

    /** Neighbours of a node in a fixed, deterministic order. */
    int[] neighbors(int node);

    /** Non-negative cost of moving along the edge from → to. */
    int cost(int from, int to);

    /** Human-readable name of a node, e.g. "(3, 4)". */
    String label(int node);
}
