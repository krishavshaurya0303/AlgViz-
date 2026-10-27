package com.algoviz.engine.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * General directed weighted graph stored as adjacency lists.
 * Used to verify Dijkstra on arbitrary weighted graphs. Negative weights are
 * rejected because Dijkstra's greedy choice is only correct for weights ≥ 0.
 */
public final class AdjacencyListGraph implements Graph {

    private record Edge(int to, int weight) {
    }

    private final List<List<Edge>> adjacency = new ArrayList<>();
    private final boolean[] blocked;

    public AdjacencyListGraph(int nodeCount) {
        if (nodeCount < 1) {
            throw new IllegalArgumentException("Graph needs at least one node");
        }
        for (int i = 0; i < nodeCount; i++) {
            adjacency.add(new ArrayList<>());
        }
        blocked = new boolean[nodeCount];
    }

    public AdjacencyListGraph addEdge(int from, int to, int weight) {
        if (weight < 0) {
            throw new IllegalArgumentException("Negative edge weights are not supported (Dijkstra requires weights >= 0)");
        }
        checkNode(from);
        checkNode(to);
        adjacency.get(from).add(new Edge(to, weight));
        return this;
    }

    public AdjacencyListGraph addUndirectedEdge(int a, int b, int weight) {
        addEdge(a, b, weight);
        return addEdge(b, a, weight);
    }

    public void block(int node) {
        checkNode(node);
        blocked[node] = true;
    }

    @Override
    public int nodeCount() {
        return adjacency.size();
    }

    @Override
    public boolean isBlocked(int node) {
        return blocked[node];
    }

    @Override
    public int[] neighbors(int node) {
        return adjacency.get(node).stream().mapToInt(Edge::to).toArray();
    }

    /** Cheapest edge from → to (parallel edges are allowed). */
    @Override
    public int cost(int from, int to) {
        int best = Integer.MAX_VALUE;
        for (Edge e : adjacency.get(from)) {
            if (e.to() == to) {
                best = Math.min(best, e.weight());
            }
        }
        if (best == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("No edge " + from + " -> " + to);
        }
        return best;
    }

    @Override
    public String label(int node) {
        return "node " + node;
    }

    private void checkNode(int node) {
        if (node < 0 || node >= adjacency.size()) {
            throw new IndexOutOfBoundsException("Node " + node + " does not exist");
        }
    }
}
