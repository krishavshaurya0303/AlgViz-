package com.algoviz.engine.graph;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.StepStatus;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Iterative Depth-First Search with an explicit stack. A node is marked visited
 * when it is popped; each stack entry remembers the node that pushed it so the
 * parent pointers describe the DFS tree actually followed.
 */
public final class DepthFirstSearch extends GraphSearchAlgorithm {

    private static final int L_SKIP = 3;
    private static final int L_VISIT = 4;
    private static final int L_TARGET = 5;
    private static final int L_PUSH = 7;
    private static final int L_NO_PATH = 8;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "dfs", "Depth-First Search", AlgorithmCategory.GRAPH,
            "Follows one branch as deep as possible before backtracking to try another.",
            "Reachability, cycle detection, topological sorting, maze generation. Does NOT find shortest paths.",
            List.of(
                    "Push the start node onto a LIFO stack.",
                    "Pop the most recently pushed node; skip it if it was already visited.",
                    "Mark it visited and push its unvisited neighbours.",
                    "Because the newest node is always expanded next, the search dives deep and backtracks only "
                            + "when it reaches a dead end."),
            List.of(
                    "stack = [start]",
                    "while stack is not empty:",
                    "    node = stack.pop()",
                    "    if node is visited: continue",
                    "    mark node visited",
                    "    if node == target: rebuild path and stop",
                    "    for each neighbour (left, down, right, up):",
                    "        if not visited and not a wall: push (neighbour, parent = node)",
                    "report \"no path\""),
            "O(V + E)", "O(V + E)", "O(V + E)", "O(V + E) stack entries", null, null, null,
            "V = cells, E = edges. A node can be pushed once per incoming edge, so the stack can hold up to E entries. "
                    + "The path DFS finds is usually not the shortest.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected boolean search(SearchInput in, GraphRecorder r, int[] parent) {
        Graph g = in.graph();
        boolean[] visited = new boolean[g.nodeCount()];
        ArrayDeque<int[]> stack = new ArrayDeque<>(); // {node, parent}
        stack.push(new int[]{in.start(), -1});
        r.discover(in.start());
        r.record(0, "Push the start node " + g.label(in.start()) + " onto the stack.", StepStatus.INFO);

        while (!stack.isEmpty()) {
            int[] entry = stack.pop();
            int node = entry[0];
            if (visited[node]) {
                r.record(L_SKIP, "Popped " + g.label(node) + ", but it was already visited through another branch. Skip it.",
                        StepStatus.INFO);
                continue;
            }
            visited[node] = true;
            parent[node] = entry[1];
            r.expand(node);
            if (node == in.target()) {
                r.record(L_TARGET, "Popped " + g.label(node) + " — this is the target.", StepStatus.SUCCESS);
                return true;
            }
            r.record(L_VISIT, "Pop " + g.label(node) + " (the most recently pushed node) and mark it visited. "
                    + "DFS explores one branch deeply before backtracking.", StepStatus.ACTIVE);

            int[] nbs = g.neighbors(node);
            int pushed = 0;
            // Push in reverse so the first neighbour in the graph's order is popped first.
            for (int k = nbs.length - 1; k >= 0; k--) {
                int nb = nbs[k];
                if (!visited[nb] && !g.isBlocked(nb)) {
                    stack.push(new int[]{nb, node});
                    r.discover(nb);
                    pushed++;
                }
            }
            String text = pushed == 0
                    ? "Dead end at " + g.label(node) + ": no unvisited neighbours. DFS will backtrack to the next node on the stack."
                    : "Pushed " + pushed + " unvisited neighbour" + (pushed == 1 ? "" : "s") + ". The last one pushed will be explored next.";
            r.record(L_PUSH, text, StepStatus.INFO);
        }
        return false;
    }

    @Override
    protected String introduction() {
        return "DFS dives down one branch using a stack.";
    }

    @Override
    protected String pathSummary() {
        return "DFS found a path, but it is not guaranteed to be the shortest.";
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
