package com.algoviz.engine.graph;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.StepStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Breadth-First Search using a FIFO queue. Ignores cell weights (every move counts as one). */
public final class BreadthFirstSearch extends GraphSearchAlgorithm {

    private static final int L_DEQUEUE = 2;
    private static final int L_TARGET = 3;
    private static final int L_DISCOVER = 6;
    private static final int L_NO_PATH = 7;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "bfs", "Breadth-First Search", AlgorithmCategory.GRAPH,
            "Explores the graph in layers: all nodes 1 move away, then all nodes 2 moves away, and so on.",
            "Shortest paths in unweighted graphs (fewest moves), level-by-level exploration, connectivity.",
            List.of(
                    "Put the start node in a FIFO queue and mark it discovered.",
                    "Repeatedly dequeue the oldest node and examine its neighbours.",
                    "Every undiscovered, non-wall neighbour is marked discovered, remembers its parent and is enqueued.",
                    "Because nodes leave the queue in order of distance, the first time the target is dequeued "
                            + "its path uses the fewest possible moves."),
            List.of(
                    "queue = [start]; mark start discovered",
                    "while queue is not empty:",
                    "    node = queue.dequeue()",
                    "    if node == target: rebuild path and stop",
                    "    for each neighbour (up, right, down, left):",
                    "        if neighbour is not discovered and not a wall:",
                    "            mark discovered; parent = node; enqueue",
                    "report \"no path\""),
            "O(V + E)", "O(V + E)", "O(V + E)", "O(V)", null, null, null,
            "V = cells, E = edges. On a 4-directional grid E ≤ 4V, so BFS is O(V). "
                    + "BFS ignores weights: it minimises the number of moves, not the cost.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected boolean search(SearchInput in, GraphRecorder r, int[] parent) {
        Graph g = in.graph();
        boolean[] discovered = new boolean[g.nodeCount()];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(in.start());
        discovered[in.start()] = true;
        r.discover(in.start());
        r.record(0, "Enqueue the start node " + g.label(in.start()) + ".", StepStatus.INFO);

        while (!queue.isEmpty()) {
            int node = queue.poll();
            r.expand(node);
            if (node == in.target()) {
                r.record(L_TARGET, "Dequeued " + g.label(node) + " — this is the target.", StepStatus.SUCCESS);
                return true;
            }
            r.record(L_DEQUEUE, "Dequeue " + g.label(node) + ", the oldest node in the queue. "
                    + "BFS visits the nearest unvisited nodes before nodes farther away.", StepStatus.ACTIVE);
            List<Integer> found = new ArrayList<>();
            for (int nb : g.neighbors(node)) {
                if (!discovered[nb] && !g.isBlocked(nb)) {
                    discovered[nb] = true;
                    parent[nb] = node;
                    queue.add(nb);
                    r.discover(nb);
                    found.add(nb);
                }
            }
            String text = found.isEmpty()
                    ? "No new neighbours: every neighbour of " + g.label(node) + " is a wall, outside the grid or already discovered."
                    : "Discovered " + labels(g, found) + ". They join the back of the queue (queue size " + queue.size() + ").";
            r.record(L_DISCOVER, text, StepStatus.INFO);
        }
        return false;
    }

    @Override
    protected String introduction() {
        return "BFS explores outward in layers using a queue.";
    }

    @Override
    protected String pathSummary() {
        return "BFS guarantees the fewest moves; weighted cells are treated like normal cells.";
    }

    @Override
    protected int pathLine() {
        return L_TARGET;
    }

    @Override
    protected int noPathLine() {
        return L_NO_PATH;
    }
}
