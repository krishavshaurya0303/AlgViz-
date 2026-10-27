package com.algoviz.engine.graph;

/** Search state of a node at one step. */
public enum NodeVisual {
    UNSEEN,
    /** Discovered and waiting in the queue / stack / priority queue. */
    FRONTIER,
    /** Removed from the frontier and expanded. */
    VISITED,
    /** The node being expanded in this step. */
    CURRENT,
    /** Part of the final path. */
    PATH
}
