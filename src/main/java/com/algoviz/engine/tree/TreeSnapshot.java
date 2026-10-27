package com.algoviz.engine.tree;

import java.util.ArrayList;
import java.util.List;

/** Immutable copy of a tree's structure at one instant. */
public final class TreeSnapshot {

    /** Immutable node of a snapshot. */
    public record Node(int id, int value, Node left, Node right) {
    }

    private static final TreeSnapshot EMPTY = new TreeSnapshot(null);

    private final Node root;
    private final int size;
    private final int height;

    private TreeSnapshot(Node root) {
        this.root = root;
        this.size = count(root);
        this.height = heightOf(root);
    }

    static TreeSnapshot of(TreeNode root) {
        return root == null ? EMPTY : new TreeSnapshot(copy(root));
    }

    public static TreeSnapshot empty() {
        return EMPTY;
    }

    public Node root() {
        return root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return size;
    }

    /** Edges on the longest root-to-leaf path; -1 for an empty tree. */
    public int height() {
        return height;
    }

    /** Nodes in inorder (sorted) order. */
    public List<Node> inorder() {
        List<Node> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    public List<Integer> inorderValues() {
        return inorder().stream().map(Node::value).toList();
    }

    private static void inorder(Node n, List<Node> out) {
        if (n == null) {
            return;
        }
        inorder(n.left(), out);
        out.add(n);
        inorder(n.right(), out);
    }

    private static Node copy(TreeNode n) {
        return n == null ? null : new Node(n.id, n.value, copy(n.left), copy(n.right));
    }

    private static int count(Node n) {
        return n == null ? 0 : 1 + count(n.left()) + count(n.right());
    }

    private static int heightOf(Node n) {
        return n == null ? -1 : 1 + Math.max(heightOf(n.left()), heightOf(n.right()));
    }
}
