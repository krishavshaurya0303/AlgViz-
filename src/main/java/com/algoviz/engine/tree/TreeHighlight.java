package com.algoviz.engine.tree;

/** How a node is emphasised at one step. */
public enum TreeHighlight {
    /** The node being examined right now. */
    CURRENT,
    /** On the path followed so far (or on the recursion stack during traversal). */
    PATH,
    /** Search hit. */
    FOUND,
    /** Newly inserted node. */
    INSERTED,
    /** Node about to be deleted. */
    DELETE_TARGET,
    /** Inorder successor used in a two-children deletion. */
    SUCCESSOR,
    /** Already output by a traversal. */
    VISITED
}
