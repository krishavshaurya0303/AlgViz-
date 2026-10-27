package com.algoviz.service;

import com.algoviz.engine.graph.BreadthFirstSearch;
import com.algoviz.engine.graph.DepthFirstSearch;
import com.algoviz.engine.graph.Dijkstra;
import com.algoviz.engine.graph.GraphSearchAlgorithm;
import com.algoviz.engine.linkedlist.ListOperation;
import com.algoviz.engine.sorting.BubbleSort;
import com.algoviz.engine.sorting.InsertionSort;
import com.algoviz.engine.sorting.MergeSort;
import com.algoviz.engine.sorting.QuickSort;
import com.algoviz.engine.sorting.SelectionSort;
import com.algoviz.engine.sorting.SortingAlgorithm;
import com.algoviz.engine.tree.TreeOperation;
import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.ComplexityEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Registry of all algorithm strategies and their documentation.
 * Controllers ask the catalog for the available strategies instead of
 * instantiating concrete classes, so adding an algorithm only touches this file.
 */
public final class AlgorithmCatalog {

    private final List<SortingAlgorithm> sorting = List.of(
            new BubbleSort(), new SelectionSort(), new InsertionSort(), new MergeSort(), new QuickSort());

    private final List<GraphSearchAlgorithm> graph = List.of(
            new BreadthFirstSearch(), new DepthFirstSearch(), new Dijkstra());

    private final AlgorithmMetadata bst = new AlgorithmMetadata(
            "bst", "Binary Search Tree", AlgorithmCategory.TREE,
            "A binary tree where every node's left subtree holds smaller values and its right subtree larger values.",
            "Ordered data with fast search, insertion and deletion when the tree stays reasonably balanced.",
            List.of(
                    "Each comparison discards one whole subtree, so operations follow a single root-to-leaf path.",
                    "Insert walks down like a search and attaches the value where the walk falls off the tree.",
                    "Delete handles three cases: a leaf is removed, a node with one child is replaced by that child, "
                            + "and a node with two children takes its inorder successor's value.",
                    "Inorder traversal of a BST visits the values in sorted order.",
                    "This is a standard BST: it does NOT rebalance itself. Inserting sorted values builds a "
                            + "'linked list' shaped tree with height n − 1."),
            TreeOperation.DELETE.pseudocode(),
            "O(1)", "O(log n)", "O(n)", "O(n) nodes; O(h) recursion for traversals", null, null,
            List.of(
                    new ComplexityEntry("Search", "O(log n)", "O(n)", "h comparisons where h is the height"),
                    new ComplexityEntry("Insert", "O(log n)", "O(n)", "walks one root-to-leaf path"),
                    new ComplexityEntry("Delete", "O(log n)", "O(n)", "find + possibly find successor"),
                    new ComplexityEntry("Traversal (any order)", "O(n)", "O(n)", "every node visited once")),
            "Average case assumes random insertion order (height ≈ log n). The worst case is a degenerate tree of height n − 1.");

    private final AlgorithmMetadata linkedList = new AlgorithmMetadata(
            "singly-linked-list", "Singly Linked List", AlgorithmCategory.LINKED_LIST,
            "A chain of nodes where each node stores a value and a pointer to the next node; the last points to null.",
            "Frequent insertions/deletions at the front, and as a building block for stacks, queues and hash buckets.",
            List.of(
                    "The list is only reachable through the head pointer.",
                    "There is no random access: reaching index i means following i next pointers.",
                    "Inserting or deleting only redirects pointers — no elements are shifted.",
                    "Order of pointer updates matters: link the new node first, then redirect the old one, "
                            + "otherwise the rest of the list is lost."),
            ListOperation.REVERSE.pseudocode(),
            "O(1)", "O(n)", "O(n)", "O(n) nodes; O(1) extra per operation", null, null,
            buildListOps(),
            "Complexities assume only a head pointer is stored (no tail pointer), as in this implementation.");

    private static List<ComplexityEntry> buildListOps() {
        List<ComplexityEntry> out = new ArrayList<>();
        for (ListOperation op : ListOperation.values()) {
            out.add(new ComplexityEntry(op.label(), op.time(), op.time(), note(op)));
        }
        return out;
    }

    private static String note(ListOperation op) {
        return switch (op) {
            case INSERT_HEAD, DELETE_HEAD -> "only the head pointer changes";
            case INSERT_TAIL, DELETE_TAIL -> "must walk to the end (no tail pointer)";
            case INSERT_AT, DELETE_AT -> "walk to index − 1, then relink";
            case SEARCH -> "linear scan";
            case TRAVERSE -> "visits every node";
            case REVERSE -> "one pass, O(1) extra space";
            case CLEAR -> "drop the head reference";
        };
    }

    public List<SortingAlgorithm> sortingAlgorithms() {
        return sorting;
    }

    public List<GraphSearchAlgorithm> graphAlgorithms() {
        return graph;
    }

    public AlgorithmMetadata binarySearchTree() {
        return bst;
    }

    public AlgorithmMetadata singlyLinkedList() {
        return linkedList;
    }

    /** Every documented entry, grouped by category order. */
    public List<AlgorithmMetadata> all() {
        List<AlgorithmMetadata> out = new ArrayList<>();
        sorting.forEach(a -> out.add(a.metadata()));
        graph.forEach(a -> out.add(a.metadata()));
        out.add(bst);
        out.add(linkedList);
        return Collections.unmodifiableList(out);
    }

    public Optional<AlgorithmMetadata> byId(String id) {
        return all().stream().filter(m -> m.id().equals(id)).findFirst();
    }

    public Optional<SortingAlgorithm> sortingById(String id) {
        return sorting.stream().filter(a -> a.metadata().id().equals(id)).findFirst();
    }

    public Optional<GraphSearchAlgorithm> graphById(String id) {
        return graph.stream().filter(a -> a.metadata().id().equals(id)).findFirst();
    }
}
