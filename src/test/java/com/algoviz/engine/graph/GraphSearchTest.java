package com.algoviz.engine.graph;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.MetricType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphSearchTest {

    static Stream<GraphSearchAlgorithm> algorithms() {
        return Stream.of(new BreadthFirstSearch(), new DepthFirstSearch(), new Dijkstra());
    }

    private static GridGraph grid(int rows, int cols, int startR, int startC, int targetR, int targetC) {
        GridGraph g = new GridGraph(rows, cols, 5);
        g.setTarget(g.index(rows - 1, cols - 1));
        g.setStart(g.index(startR, startC));
        g.setTarget(g.index(targetR, targetC));
        return g;
    }

    /** Asserts the path starts/ends correctly, uses adjacent cells only and avoids walls. */
    private static void assertValidPath(GridGraph g, List<Integer> path) {
        assertEquals(g.start(), path.get(0));
        assertEquals(g.target(), path.get(path.size() - 1));
        for (int i = 0; i < path.size(); i++) {
            assertFalse(g.isWall(path.get(i)), "path crosses a wall");
            if (i > 0) {
                int a = path.get(i - 1);
                int b = path.get(i);
                int dist = Math.abs(g.rowOf(a) - g.rowOf(b)) + Math.abs(g.colOf(a) - g.colOf(b));
                assertEquals(1, dist, "path must move to a 4-directional neighbour");
            }
        }
        assertEquals(path.size(), path.stream().distinct().count(), "path must not repeat cells");
    }

    /** Independent reference BFS distance. */
    private static int referenceHops(GridGraph g) {
        int[] d = new int[g.nodeCount()];
        Arrays.fill(d, -1);
        ArrayDeque<Integer> q = new ArrayDeque<>();
        d[g.start()] = 0;
        q.add(g.start());
        while (!q.isEmpty()) {
            int u = q.poll();
            for (int v : g.neighbors(u)) {
                if (d[v] < 0 && !g.isWall(v)) {
                    d[v] = d[u] + 1;
                    q.add(v);
                }
            }
        }
        return d[g.target()];
    }

    /** Independent reference Dijkstra (O(V^2)). */
    private static long referenceCost(Graph g, int s, int t) {
        long[] d = new long[g.nodeCount()];
        Arrays.fill(d, Long.MAX_VALUE);
        boolean[] done = new boolean[g.nodeCount()];
        d[s] = 0;
        for (int it = 0; it < g.nodeCount(); it++) {
            int u = -1;
            for (int i = 0; i < g.nodeCount(); i++) {
                if (!done[i] && d[i] != Long.MAX_VALUE && (u < 0 || d[i] < d[u])) {
                    u = i;
                }
            }
            if (u < 0) {
                break;
            }
            done[u] = true;
            for (int v : g.neighbors(u)) {
                if (!g.isBlocked(v) && d[u] + g.cost(u, v) < d[v]) {
                    d[v] = d[u] + g.cost(u, v);
                }
            }
        }
        return d[t] == Long.MAX_VALUE ? -1 : d[t];
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void findsValidPathOnOpenGrid(GraphSearchAlgorithm alg) {
        GridGraph g = grid(5, 7, 2, 0, 2, 6);
        ExecutionTrace<GraphStep> t = alg.run(new SearchInput(g, g.start(), g.target()));
        assertEquals(SearchOutcome.FOUND, t.last().outcome());
        assertValidPath(g, t.last().path());
        assertEquals(t.last().path().size() - 1, t.last().metrics().get(MetricType.PATH_LENGTH));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void reportsNoPathWhenTargetIsWalledOff(GraphSearchAlgorithm alg) {
        GridGraph g = grid(5, 5, 0, 0, 4, 4);
        for (int r = 0; r < 5; r++) {
            g.setWall(g.index(r, 2), true);
        }
        ExecutionTrace<GraphStep> t = alg.run(new SearchInput(g, g.start(), g.target()));
        assertEquals(SearchOutcome.NO_PATH, t.last().outcome());
        assertTrue(t.last().path().isEmpty());
        // It explored exactly the reachable left region (columns 0-1).
        assertEquals(10, t.last().metrics().get(MetricType.NODES_EXPLORED));
        for (int r = 0; r < 5; r++) {
            for (int c = 3; c < 5; c++) {
                assertEquals(NodeVisual.UNSEEN, t.last().stateOf(g.index(r, c)));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void startEqualsTargetGivesZeroLengthPath(GraphSearchAlgorithm alg) {
        AdjacencyListGraph g = new AdjacencyListGraph(3);
        g.addUndirectedEdge(0, 1, 1).addUndirectedEdge(1, 2, 1);
        ExecutionTrace<GraphStep> t = alg.run(new SearchInput(g, 1, 1));
        assertEquals(SearchOutcome.FOUND, t.last().outcome());
        assertEquals(List.of(1), t.last().path());
        assertEquals(0, t.last().metrics().get(MetricType.PATH_LENGTH));
        assertEquals(1, t.last().metrics().get(MetricType.NODES_EXPLORED));
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void routesAroundBlockedCorridor(GraphSearchAlgorithm alg) {
        // Wall in column 2 with a single gap at the bottom row.
        GridGraph g = grid(4, 5, 0, 0, 0, 4);
        for (int r = 0; r < 3; r++) {
            g.setWall(g.index(r, 2), true);
        }
        List<Integer> path = alg.findPath(new SearchInput(g, g.start(), g.target()));
        assertValidPath(g, path);
        assertTrue(path.contains(g.index(3, 2)), "the only gap must be used");
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void respectsBoundariesFromCorners(GraphSearchAlgorithm alg) {
        GridGraph g = grid(3, 3, 0, 0, 2, 2);
        List<Integer> path = alg.findPath(new SearchInput(g, g.start(), g.target()));
        assertValidPath(g, path);
        assertEquals(2, g.neighbors(g.index(0, 0)).length);
        assertEquals(4, g.neighbors(g.index(1, 1)).length);
    }

    @Test
    void bfsVisitsInNonDecreasingDistanceOrder() {
        GridGraph g = grid(6, 6, 0, 0, 5, 5);
        ExecutionTrace<GraphStep> t = new BreadthFirstSearch().run(new SearchInput(g, g.start(), g.target()));
        int last = 0;
        for (GraphStep s : t.steps()) {
            if (s.current() >= 0) {
                int d = g.rowOf(s.current()) + g.colOf(s.current()); // Manhattan = BFS distance on an open grid
                assertTrue(d >= last, "BFS expanded a farther node before a nearer one");
                last = d;
            }
        }
    }

    @Test
    void dfsGoesDeepFirst() {
        // On an open grid DFS (neighbour order up, right, down, left) heads right along row 0 first.
        GridGraph g = grid(4, 6, 0, 0, 3, 0);
        ExecutionTrace<GraphStep> t = new DepthFirstSearch().run(new SearchInput(g, g.start(), g.target()));
        int[] firstExpanded = t.steps().stream().filter(s -> s.current() >= 0).mapToInt(GraphStep::current)
                .distinct().limit(4).toArray();
        assertEquals(List.of(g.index(0, 0), g.index(0, 1), g.index(0, 2), g.index(0, 3)),
                Arrays.stream(firstExpanded).boxed().toList());
    }

    @Test
    void bfsFindsMinimumMovesOnRandomGrids() {
        Random rnd = new Random(7);
        for (int k = 0; k < 60; k++) {
            GridGraph g = grid(12, 18, 0, 0, 11, 17);
            g.randomize(rnd, 0.3, 0.0);
            int want = referenceHops(g);
            List<Integer> path = new BreadthFirstSearch().findPath(new SearchInput(g, g.start(), g.target()));
            if (want < 0) {
                assertTrue(path.isEmpty());
            } else {
                assertValidPath(g, path);
                assertEquals(want, path.size() - 1);
                // DFS also finds *a* path whenever one exists (but possibly longer).
                List<Integer> dfs = new DepthFirstSearch().findPath(new SearchInput(g, g.start(), g.target()));
                assertValidPath(g, dfs);
                assertTrue(dfs.size() >= path.size());
            }
        }
    }

    @Test
    void dijkstraMinimisesCostOnWeightedGrids() {
        Random rnd = new Random(11);
        for (int k = 0; k < 60; k++) {
            GridGraph g = grid(10, 14, 0, 0, 9, 13);
            g.randomize(rnd, 0.2, 0.3);
            long want = referenceCost(g, g.start(), g.target());
            ExecutionTrace<GraphStep> t = new Dijkstra().run(new SearchInput(g, g.start(), g.target()));
            if (want < 0) {
                assertEquals(SearchOutcome.NO_PATH, t.last().outcome());
            } else {
                assertValidPath(g, t.last().path());
                assertEquals(want, t.last().metrics().get(MetricType.PATH_COST));
                // BFS's path can never be cheaper than Dijkstra's.
                ExecutionTrace<GraphStep> b = new BreadthFirstSearch().run(new SearchInput(g, g.start(), g.target()));
                assertTrue(b.last().metrics().get(MetricType.PATH_COST) >= want);
            }
        }
    }

    @Test
    void dijkstraPrefersCheaperLongerRoute() {
        //   0 --10--> 3 directly, or 0 -1-> 1 -1-> 2 -1-> 3
        AdjacencyListGraph g = new AdjacencyListGraph(4);
        g.addEdge(0, 3, 10).addEdge(0, 1, 1).addEdge(1, 2, 1).addEdge(2, 3, 1);
        ExecutionTrace<GraphStep> t = new Dijkstra().run(new SearchInput(g, 0, 3));
        assertEquals(List.of(0, 1, 2, 3), t.last().path());
        assertEquals(3, t.last().metrics().get(MetricType.PATH_COST));
        assertEquals(3, t.last().distance(3));
        // BFS minimises edges instead.
        assertEquals(List.of(0, 3), new BreadthFirstSearch().findPath(new SearchInput(g, 0, 3)));
    }

    @Test
    void dijkstraHandlesZeroWeightEdges() {
        AdjacencyListGraph g = new AdjacencyListGraph(3);
        g.addEdge(0, 1, 0).addEdge(1, 2, 0).addEdge(0, 2, 1);
        ExecutionTrace<GraphStep> t = new Dijkstra().run(new SearchInput(g, 0, 2));
        assertEquals(0, t.last().metrics().get(MetricType.PATH_COST));
    }

    @Test
    void negativeWeightsAreRejected() {
        AdjacencyListGraph g = new AdjacencyListGraph(2);
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 1, -1));
    }

    @Test
    void wallsCannotBeStartOrTarget() {
        GridGraph g = grid(3, 3, 0, 0, 2, 2);
        assertFalse(g.setWall(g.start(), true));
        assertFalse(g.setWall(g.target(), true));
        assertFalse(g.setStart(g.target()));
        AdjacencyListGraph a = new AdjacencyListGraph(2);
        a.block(1);
        assertThrows(IllegalArgumentException.class, () -> new SearchInput(a, 0, 1));
    }

    @Test
    void frontierMetricMatchesFrontierCells() {
        GridGraph g = grid(6, 8, 3, 3, 0, 7);
        g.randomize(new Random(3), 0.2, 0.2);
        for (GraphSearchAlgorithm alg : algorithms().toList()) {
            for (GraphStep s : alg.run(new SearchInput(g, g.start(), g.target())).steps()) {
                long count = 0;
                for (int i = 0; i < s.nodeCount(); i++) {
                    if (s.stateOf(i) == NodeVisual.FRONTIER) {
                        count++;
                    }
                }
                assertEquals(count, s.metrics().get(MetricType.FRONTIER_SIZE), alg.name());
            }
        }
    }

    @Test
    void searchRunsOnCopySoEditsDoNotLeakIntoTrace() {
        GridGraph g = grid(3, 4, 0, 0, 0, 3);
        GridGraph copy = g.copy();
        g.setWall(g.index(0, 1), true);
        assertFalse(copy.isWall(copy.index(0, 1)));
    }
}
