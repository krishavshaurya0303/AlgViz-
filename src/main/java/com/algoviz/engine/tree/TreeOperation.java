package com.algoviz.engine.tree;

import java.util.List;

/** Operations offered by the Tree Visualizer, each with its own pseudocode. */
public enum TreeOperation {
    INSERT("Insert", List.of(
            "if root is null: root = new Node(v); return",
            "current = root",
            "loop:",
            "    if v == current.value: reject duplicate",
            "    if v < current.value:",
            "        if current.left is null: current.left = new Node(v); return",
            "        current = current.left",
            "    else:",
            "        if current.right is null: current.right = new Node(v); return",
            "        current = current.right")),
    SEARCH("Search", List.of(
            "current = root",
            "while current is not null:",
            "    if v == current.value: return FOUND",
            "    if v < current.value: current = current.left",
            "    else: current = current.right",
            "return NOT FOUND")),
    DELETE("Delete", List.of(
            "find the node holding v (same steps as search)",
            "if not found: report and stop",
            "if node has no children: unlink it from its parent",
            "else if node has one child: link its parent to that child",
            "else (two children):",
            "    successor = leftmost node of node.right",
            "    copy successor.value into node",
            "    unlink successor (it has no left child)")),
    INORDER("Inorder", List.of(
            "inorder(node):",
            "    if node is null: return",
            "    inorder(node.left)",
            "    visit(node)",
            "    inorder(node.right)")),
    PREORDER("Preorder", List.of(
            "preorder(node):",
            "    if node is null: return",
            "    visit(node)",
            "    preorder(node.left)",
            "    preorder(node.right)")),
    POSTORDER("Postorder", List.of(
            "postorder(node):",
            "    if node is null: return",
            "    postorder(node.left)",
            "    postorder(node.right)",
            "    visit(node)")),
    LEVEL_ORDER("Level-order", List.of(
            "queue = [root]",
            "while queue is not empty:",
            "    node = queue.dequeue(); visit(node)",
            "    enqueue node.left and node.right if not null"));

    private final String label;
    private final List<String> pseudocode;

    TreeOperation(String label, List<String> pseudocode) {
        this.label = label;
        this.pseudocode = pseudocode;
    }

    public String label() {
        return label;
    }

    public List<String> pseudocode() {
        return pseudocode;
    }

    public boolean isTraversal() {
        return this == INORDER || this == PREORDER || this == POSTORDER || this == LEVEL_ORDER;
    }
}
