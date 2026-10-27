package com.algoviz.engine.graph;

/** A (row, column) position on the grid. */
public record GridCell(int row, int col) {

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
