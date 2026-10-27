package com.algoviz.engine.tree;

/**
 * Mutable BST node. Package-private: only {@link BinarySearchTree} can modify nodes.
 * The id is stable for the node's lifetime so the UI can animate a node even
 * when its value changes (two-children deletion copies the successor's value).
 */
final class TreeNode {
    final int id;
    int value;
    TreeNode left;
    TreeNode right;

    TreeNode(int id, int value) {
        this.id = id;
        this.value = value;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }
}
