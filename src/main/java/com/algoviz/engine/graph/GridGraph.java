package com.algoviz.engine.graph;

import java.util.Arrays;
import java.util.Random;

/**
 * Editable grid graph with 4-directional movement (up, right, down, left).
 * Diagonal moves are not allowed. Entering a normal cell costs 1; entering a
 * weighted cell costs {@link #weightedCost()}. Walls cannot be entered.
 */
public final class GridGraph implements Graph {

    private static final int[] DR = {-1, 0, 1, 0};
    private static final int[] DC = {0, 1, 0, -1};

    private final int rows;
    private final int cols;
    private final int weightedCost;
    private final boolean[] walls;
    private final boolean[] weighted;
    private int start;
    private int target;

    public GridGraph(int rows, int cols, int weightedCost) {
        if (rows < 1 || cols < 1 || rows * cols < 2) {
            throw new IllegalArgumentException("Grid must contain at least two cells");
        }
        if (weightedCost < 1) {
            throw new IllegalArgumentException("Weighted cost must be at least 1");
        }
        this.rows = rows;
        this.cols = cols;
        this.weightedCost = weightedCost;
        this.walls = new boolean[rows * cols];
        this.weighted = new boolean[rows * cols];
        this.start = index(rows / 2, cols / 4);
        this.target = index(rows / 2, cols - 1 - cols / 4);
        if (start == target) {
            target = start == 0 ? 1 : 0;
        }
    }

    private GridGraph(GridGraph other) {
        this.rows = other.rows;
        this.cols = other.cols;
        this.weightedCost = other.weightedCost;
        this.walls = other.walls.clone();
        this.weighted = other.weighted.clone();
        this.start = other.start;
        this.target = other.target;
    }

    /** Independent copy; searches run on a copy so later edits cannot affect a trace. */
    public GridGraph copy() {
        return new GridGraph(this);
    }

    // ------------------------------------------------------------ geometry

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public int index(int row, int col) {
        if (!inBounds(row, col)) {
            throw new IndexOutOfBoundsException("Cell (" + row + ", " + col + ") is outside the grid");
        }
        return row * cols + col;
    }

    public int rowOf(int node) {
        return node / cols;
    }

    public int colOf(int node) {
        return node % cols;
    }

    public GridCell cellOf(int node) {
        return new GridCell(rowOf(node), colOf(node));
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    // --------------------------------------------------------------- Graph

    @Override
    public int nodeCount() {
        return rows * cols;
    }

    @Override
    public boolean isBlocked(int node) {
        return walls[node];
    }

    @Override
    public int[] neighbors(int node) {
        int r = rowOf(node);
        int c = colOf(node);
        int[] buf = new int[4];
        int n = 0;
        for (int d = 0; d < 4; d++) {
            int nr = r + DR[d];
            int nc = c + DC[d];
            if (inBounds(nr, nc)) {
                buf[n++] = nr * cols + nc;
            }
        }
        return Arrays.copyOf(buf, n);
    }

    /** Cost of entering {@code to}; independent of the direction of travel. */
    @Override
    public int cost(int from, int to) {
        return weighted[to] ? weightedCost : 1;
    }

    @Override
    public String label(int node) {
        return cellOf(node).toString();
    }

    // ------------------------------------------------------------- editing

    public int start() {
        return start;
    }

    public int target() {
        return target;
    }

    public int weightedCost() {
        return weightedCost;
    }

    public boolean isWall(int node) {
        return walls[node];
    }

    public boolean isWeighted(int node) {
        return weighted[node];
    }

    /** Moves the start. Returns false if the cell is the target. Clears any wall/weight there. */
    public boolean setStart(int node) {
        if (node == target) {
            return false;
        }
        start = node;
        walls[node] = false;
        weighted[node] = false;
        return true;
    }

    public boolean setTarget(int node) {
        if (node == start) {
            return false;
        }
        target = node;
        walls[node] = false;
        weighted[node] = false;
        return true;
    }

    /** Places or removes a wall. Start and target can never become walls. */
    public boolean setWall(int node, boolean wall) {
        if (wall && (node == start || node == target)) {
            return false;
        }
        walls[node] = wall;
        if (wall) {
            weighted[node] = false;
        }
        return true;
    }

    public boolean setWeighted(int node, boolean value) {
        if (value && (node == start || node == target)) {
            return false;
        }
        weighted[node] = value;
        if (value) {
            walls[node] = false;
        }
        return true;
    }

    public void clearObstacles() {
        Arrays.fill(walls, false);
        Arrays.fill(weighted, false);
    }

    public void clearWeights() {
        Arrays.fill(weighted, false);
    }

    /** Scatters random walls (and optionally weighted cells) with the given densities. */
    public void randomize(Random random, double wallDensity, double weightDensity) {
        clearObstacles();
        for (int i = 0; i < nodeCount(); i++) {
            if (i == start || i == target) {
                continue;
            }
            double roll = random.nextDouble();
            if (roll < wallDensity) {
                walls[i] = true;
            } else if (roll < wallDensity + weightDensity) {
                weighted[i] = true;
            }
        }
    }

    public int wallCount() {
        int n = 0;
        for (boolean w : walls) {
            if (w) {
                n++;
            }
        }
        return n;
    }

    public int weightedCount() {
        int n = 0;
        for (boolean w : weighted) {
            if (w) {
                n++;
            }
        }
        return n;
    }
}
