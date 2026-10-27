package com.algoviz.engine.graph;

import com.algoviz.engine.Algorithm;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.StepStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Template for the graph searches. Subclasses implement the exploration
 * strategy; this class records the initial state, reconstructs the path from
 * parent pointers and records the final "found" / "no path" outcome.
 */
public abstract class GraphSearchAlgorithm implements Algorithm<SearchInput, GraphStep> {

    @Override
    public final ExecutionTrace<GraphStep> run(SearchInput input) {
        long t0 = System.nanoTime();
        Graph g = input.graph();
        GraphRecorder r = new GraphRecorder(g, tracksDistances());
        r.record(-1, "Search from " + g.label(input.start()) + " to " + g.label(input.target()) + ". "
                + introduction(), StepStatus.INFO);

        int[] parent = new int[g.nodeCount()];
        Arrays.fill(parent, -1);
        boolean found = search(input, r, parent);

        if (found) {
            tracePath(input, r, parent);
        } else {
            r.noPath();
            r.record(noPathLine(), "The frontier is empty and the target was never reached. "
                    + "No path exists — the walls cut the target off from the start.", StepStatus.FAILURE);
        }
        return new ExecutionTrace<>(name(), r.steps(), System.nanoTime() - t0);
    }

    /** Convenience for tests and callers that only need the resulting path (empty if none). */
    public final List<Integer> findPath(SearchInput input) {
        return run(input).last().path();
    }

    private void tracePath(SearchInput input, GraphRecorder r, int[] parent) {
        Graph g = input.graph();
        List<Integer> backwards = new ArrayList<>();
        for (int v = input.target(); v != -1; v = parent[v]) {
            backwards.add(v);
        }
        r.beginPath();
        long cost = 0;
        for (int i = 0; i < backwards.size(); i++) {
            int node = backwards.get(i);
            r.addPathNode(node);
            if (i + 1 < backwards.size()) {
                cost += g.cost(backwards.get(i + 1), node);
            }
            String text = i == 0
                    ? "Target reached! Now follow the parent pointers back from the target to rebuild the path."
                    : "The parent of " + g.label(backwards.get(i - 1)) + " is " + g.label(node) + ".";
            r.record(pathLine(), text, StepStatus.SUCCESS);
        }
        List<Integer> ordered = new ArrayList<>(backwards);
        Collections.reverse(ordered);
        r.setPathOrder(ordered);
        int length = ordered.size() - 1;
        r.setPathMetrics(length, cost);
        r.record(pathLine(), "Path found: " + length + " move" + (length == 1 ? "" : "s")
                + " with total cost " + cost + ". " + pathSummary(), StepStatus.SUCCESS);
    }

    /** Runs the exploration. Must fill {@code parent} for every discovered node. */
    protected abstract boolean search(SearchInput input, GraphRecorder r, int[] parent);

    protected abstract String introduction();

    /** One-sentence remark about what the found path means for this algorithm. */
    protected abstract String pathSummary();

    protected abstract int pathLine();

    protected abstract int noPathLine();

    protected boolean tracksDistances() {
        return false;
    }

    /** Formats a list of node labels for explanations. */
    protected static String labels(Graph g, List<Integer> nodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) {
                sb.append(i == nodes.size() - 1 ? " and " : ", ");
            }
            sb.append(g.label(nodes.get(i)));
        }
        return sb.toString();
    }
}
