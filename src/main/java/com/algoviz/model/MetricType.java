package com.algoviz.model;

/**
 * Every counter AlgoViz can report. Each constant carries a precise definition
 * which is shown as a tooltip so that users know exactly what is being counted.
 */
public enum MetricType {
    COMPARISONS("Comparisons",
            "Number of times two element values were compared with each other."),
    SWAPS("Swaps",
            "Number of exchanges of two array positions. Shifts are not swaps."),
    WRITES("Writes",
            "Number of individual assignments into the array. A swap counts as two writes."),
    NODES_EXPLORED("Nodes explored",
            "Number of nodes removed from the frontier and expanded (visited)."),
    FRONTIER_SIZE("Frontier size",
            "Number of discovered nodes currently waiting in the queue / stack / priority queue."),
    PATH_LENGTH("Path length",
            "Number of moves (edges) on the final path from start to target."),
    PATH_COST("Path cost",
            "Sum of the cost of entering each cell on the final path. Normal cells cost 1, weighted cells cost more."),
    NODES_VISITED("Nodes visited",
            "Number of nodes the operation has examined so far."),
    TREE_HEIGHT("Tree height",
            "Number of edges on the longest root-to-leaf path. An empty tree has height -1, a single node has height 0."),
    NODE_COUNT("Nodes",
            "Number of nodes currently in the structure."),
    POINTER_CHANGES("Pointer changes",
            "Number of next/head pointer assignments performed by the operation.");

    private final String label;
    private final String description;

    MetricType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }
}
