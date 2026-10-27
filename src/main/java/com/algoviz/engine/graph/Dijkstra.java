package com.algoviz.engine.graph;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.StepStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Dijkstra's shortest-path algorithm with a binary-heap priority queue and lazy
 * deletion (outdated queue entries are skipped). Requires non-negative weights.
 */
public final class Dijkstra extends GraphSearchAlgorithm {

    private static final int L_INIT = 0;
    private static final int L_EXTRACT = 2;
    private static final int L_SKIP = 3;
    private static final int L_TARGET = 5;
    private static final int L_RELAX = 8;
    private static final int L_NO_PATH = 9;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "dijkstra", "Dijkstra's Algorithm", AlgorithmCategory.GRAPH,
            "Finds the cheapest path in a graph with non-negative edge weights by always settling the closest unsettled node.",
            "Road networks, routing, any shortest-path problem with non-negative costs.",
            List.of(
                    "Every node starts at distance ∞ except the start, which is 0.",
                    "Repeatedly extract the unsettled node with the smallest known distance and settle it.",
                    "Relax its edges: if going through it is cheaper, update the neighbour's distance and parent.",
                    "With non-negative weights, a settled node's distance can never improve later, so it is final."),
            List.of(
                    "dist[*] = ∞; dist[start] = 0; pq = [(0, start)]",
                    "while pq is not empty:",
                    "    (d, node) = pq.extractMin()",
                    "    if node is settled: continue",
                    "    settle node",
                    "    if node == target: rebuild path and stop",
                    "    for each neighbour of node:",
                    "        if dist[node] + cost < dist[neighbour]:",
                    "            dist[neighbour] = dist[node] + cost; parent = node; pq.insert",
                    "report \"no path\""),
            "O((V + E) log V)", "O((V + E) log V)", "O((V + E) log V)", "O(V + E)", null, null, null,
            "With a binary heap. On a grid where every cell costs 1, Dijkstra explores like BFS and finds an "
                    + "equally short path — it only has an advantage when weights differ.");

    /** Priority-queue entry; seq breaks ties in insertion order so runs are deterministic. */
    private record Entry(int distance, long seq, int node) implements Comparable<Entry> {
        @Override
        public int compareTo(Entry o) {
            int c = Integer.compare(distance, o.distance);
            return c != 0 ? c : Long.compare(seq, o.seq);
        }
    }

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected boolean tracksDistances() {
        return true;
    }

    @Override
    protected boolean search(SearchInput in, GraphRecorder r, int[] parent) {
        Graph g = in.graph();
        int n = g.nodeCount();
        int[] dist = new int[n];
        Arrays.fill(dist, Integer.MAX_VALUE);
        boolean[] settled = new boolean[n];
        PriorityQueue<Entry> pq = new PriorityQueue<>();
        long seq = 0;

        dist[in.start()] = 0;
        r.setDistance(in.start(), 0);
        pq.add(new Entry(0, seq++, in.start()));
        r.discover(in.start());
        r.record(L_INIT, "Every distance starts at ∞ except the start " + g.label(in.start()) + ", which is 0.",
                StepStatus.INFO);

        while (!pq.isEmpty()) {
            Entry e = pq.poll();
            int node = e.node();
            if (settled[node]) {
                r.record(L_SKIP, "Extracted an outdated entry for " + g.label(node) + " (distance " + e.distance()
                        + "); it is already settled with distance " + dist[node] + ". Skip it.", StepStatus.INFO);
                continue;
            }
            settled[node] = true;
            r.expand(node);
            if (node == in.target()) {
                r.record(L_TARGET, "Settled the target " + g.label(node) + " with distance " + dist[node] + ".",
                        StepStatus.SUCCESS);
                return true;
            }
            r.record(L_EXTRACT, "Select " + g.label(node) + ", the unsettled node with the smallest known distance ("
                    + dist[node] + "), and settle it. Its distance is now final.", StepStatus.ACTIVE);

            List<String> updates = new ArrayList<>();
            for (int nb : g.neighbors(node)) {
                if (settled[nb] || g.isBlocked(nb)) {
                    continue;
                }
                int cost = g.cost(node, nb);
                if (cost < 0) {
                    throw new IllegalStateException("Dijkstra requires non-negative edge weights");
                }
                long candidate = (long) dist[node] + cost;
                if (candidate < dist[nb]) {
                    String before = dist[nb] == Integer.MAX_VALUE ? "∞" : String.valueOf(dist[nb]);
                    dist[nb] = (int) candidate;
                    parent[nb] = node;
                    pq.add(new Entry(dist[nb], seq++, nb));
                    r.setDistance(nb, dist[nb]);
                    r.discover(nb);
                    updates.add(g.label(nb) + ": " + before + " → " + dist[nb]);
                }
            }
            String text = updates.isEmpty()
                    ? "No distance improved through " + g.label(node) + "."
                    : "Relaxed edges from " + g.label(node) + ". Improved: " + String.join(", ", updates) + ".";
            r.record(L_RELAX, text, StepStatus.INFO);
        }
        return false;
    }

    @Override
    protected String introduction() {
        return "Dijkstra always settles the unsettled node with the smallest known distance.";
    }

    @Override
    protected String pathSummary() {
        return "Dijkstra guarantees the minimum total cost (weighted cells included).";
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
