package com.algoviz.engine.graph;

import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.MetricType;
import com.algoviz.model.StepStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tracks the visual state of every node while a search runs and records an
 * immutable {@link GraphStep} whenever the algorithm reaches a meaningful point.
 */
public final class GraphRecorder {

    private final Graph graph;
    private final byte[] states;
    private final int[] distances;
    private final List<Integer> path = new ArrayList<>();
    private final List<GraphStep> steps = new ArrayList<>();
    private final ExecutionMetrics.Counter counter = new ExecutionMetrics.Counter()
            .track(MetricType.NODES_EXPLORED, MetricType.FRONTIER_SIZE,
                    MetricType.PATH_LENGTH, MetricType.PATH_COST);
    private SearchOutcome outcome = SearchOutcome.SEARCHING;
    private int current = -1;
    private int frontier;

    GraphRecorder(Graph graph, boolean trackDistances) {
        this.graph = graph;
        this.states = new byte[graph.nodeCount()];
        if (trackDistances) {
            distances = new int[graph.nodeCount()];
            Arrays.fill(distances, Integer.MAX_VALUE);
        } else {
            distances = null;
        }
    }

    public Graph graph() {
        return graph;
    }

    public String label(int node) {
        return graph.label(node);
    }

    public NodeVisual stateOf(int node) {
        return NodeVisual.values()[states[node]];
    }

    /** Adds a node to the frontier (if it is not already there). */
    public void discover(int node) {
        set(node, NodeVisual.FRONTIER);
    }

    /** Makes a node the one being expanded; the previous current node becomes VISITED. */
    public void expand(int node) {
        settlePrevious();
        current = node;
        set(node, NodeVisual.CURRENT);
        counter.increment(MetricType.NODES_EXPLORED);
    }

    public void setDistance(int node, int distance) {
        distances[node] = distance;
    }

    public int frontierCount() {
        return frontier;
    }

    public void record(int line, String explanation, StepStatus status) {
        counter.set(MetricType.FRONTIER_SIZE, frontier);
        steps.add(new GraphStep(states, current, distances, path, outcome, counter.snapshot(),
                line, explanation, status));
    }

    // ------------------------------------------------------------ endings

    void beginPath() {
        settlePrevious();
        current = -1;
        outcome = SearchOutcome.FOUND;
    }

    void addPathNode(int node) {
        path.add(node);
        if (states[node] == NodeVisual.FRONTIER.ordinal()) {
            frontier--;
        }
        states[node] = (byte) NodeVisual.PATH.ordinal();
    }

    void setPathOrder(List<Integer> ordered) {
        path.clear();
        path.addAll(ordered);
    }

    void setPathMetrics(int length, long cost) {
        counter.set(MetricType.PATH_LENGTH, length);
        counter.set(MetricType.PATH_COST, cost);
    }

    void noPath() {
        settlePrevious();
        current = -1;
        outcome = SearchOutcome.NO_PATH;
    }

    List<GraphStep> steps() {
        return steps;
    }

    private void settlePrevious() {
        if (current >= 0 && states[current] == NodeVisual.CURRENT.ordinal()) {
            states[current] = (byte) NodeVisual.VISITED.ordinal();
        }
    }

    private void set(int node, NodeVisual visual) {
        boolean wasFrontier = states[node] == NodeVisual.FRONTIER.ordinal();
        boolean isFrontier = visual == NodeVisual.FRONTIER;
        if (wasFrontier && !isFrontier) {
            frontier--;
        } else if (!wasFrontier && isFrontier) {
            frontier++;
        }
        states[node] = (byte) visual.ordinal();
    }
}
