package com.algoviz.engine.tree;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.MetricType;
import com.algoviz.model.StepStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A standard (unbalanced) Binary Search Tree of distinct integers.
 * <p>
 * Policy: duplicates are rejected. Every operation that the user can trigger
 * returns an {@link ExecutionTrace} describing each step. Mutating operations
 * change the tree immediately; the trace's snapshots show how it got there,
 * so stepping backward displays the earlier structure without undoing the
 * real operation. The tree does NOT balance itself.
 */
public final class BinarySearchTree {

    private TreeNode root;
    private int size;
    private int nextId = 1;

    // ----------------------------------------------------------------- queries

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return root == null;
    }

    /** Edges on the longest root-to-leaf path; -1 for an empty tree. */
    public int height() {
        return snapshot().height();
    }

    public boolean contains(int value) {
        TreeNode n = root;
        while (n != null) {
            if (value == n.value) {
                return true;
            }
            n = value < n.value ? n.left : n.right;
        }
        return false;
    }

    public TreeSnapshot snapshot() {
        return TreeSnapshot.of(root);
    }

    public List<Integer> inorderValues() {
        return snapshot().inorderValues();
    }

    /** Verifies the BST property for every node (used by tests and as a safety check). */
    public boolean isValidBst() {
        return isValid(root, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    private static boolean isValid(TreeNode n, long lo, long hi) {
        if (n == null) {
            return true;
        }
        if (n.value <= lo || n.value >= hi) {
            return false;
        }
        return isValid(n.left, lo, n.value) && isValid(n.right, n.value, hi);
    }

    // ------------------------------------------------------- silent mutation

    /** Inserts without recording (used for generating sample trees). Returns false for duplicates. */
    public boolean insertSilently(int value) {
        if (root == null) {
            root = newNode(value);
            return true;
        }
        TreeNode n = root;
        while (true) {
            if (value == n.value) {
                return false;
            }
            if (value < n.value) {
                if (n.left == null) {
                    n.left = newNode(value);
                    return true;
                }
                n = n.left;
            } else {
                if (n.right == null) {
                    n.right = newNode(value);
                    return true;
                }
                n = n.right;
            }
        }
    }

    public void clear() {
        root = null;
        size = 0;
    }

    // ------------------------------------------------------ traced operations

    /** A one-step trace showing the tree as it is now (used after sample/clear and on first show). */
    public ExecutionTrace<TreeStep> currentState(String explanation) {
        Recorder r = new Recorder(TreeOperation.VIEW);
        r.record(-1, explanation, root == null ? StepStatus.INFO : StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<TreeStep> insert(int value) {
        Recorder r = new Recorder(TreeOperation.INSERT);
        r.record(-1, "Insert " + value + ". Start at the root and compare at each node.", StepStatus.INFO);
        if (root == null) {
            root = newNode(value);
            r.mark(root, TreeHighlight.INSERTED);
            r.record(0, "The tree is empty, so " + value + " becomes the root.", StepStatus.SUCCESS);
            return r.trace();
        }
        TreeNode current = root;
        r.mark(current, TreeHighlight.CURRENT);
        r.record(1, "Start at the root " + current.value + ".", StepStatus.ACTIVE);
        while (true) {
            r.compare();
            if (value == current.value) {
                r.mark(current, TreeHighlight.FOUND);
                r.record(3, value + " is already in the tree. Duplicates are not allowed, so nothing is inserted.",
                        StepStatus.FAILURE);
                return r.trace();
            }
            boolean goLeft = value < current.value;
            TreeNode next = goLeft ? current.left : current.right;
            String dir = goLeft ? "left" : "right";
            String cmp = value + (goLeft ? " is less than " : " is greater than ") + current.value;
            if (next == null) {
                TreeNode created = newNode(value);
                if (goLeft) {
                    current.left = created;
                } else {
                    current.right = created;
                }
                r.mark(current, TreeHighlight.PATH);
                r.mark(created, TreeHighlight.INSERTED);
                r.record(goLeft ? 5 : 8, cmp + " and the " + dir + " child is empty, so " + value
                        + " is attached as the " + dir + " child of " + current.value + ".", StepStatus.SUCCESS);
                return r.trace();
            }
            r.mark(current, TreeHighlight.PATH);
            r.mark(next, TreeHighlight.CURRENT);
            r.record(goLeft ? 6 : 9, cmp + ", so we move to the " + dir + " subtree (" + next.value + ").",
                    StepStatus.ACTIVE);
            current = next;
        }
    }

    public ExecutionTrace<TreeStep> search(int value) {
        Recorder r = new Recorder(TreeOperation.SEARCH);
        r.record(-1, "Search for " + value + ", starting at the root.", StepStatus.INFO);
        TreeNode current = root;
        if (current != null) {
            r.mark(current, TreeHighlight.CURRENT);
            r.record(0, "Start at the root " + current.value + ".", StepStatus.ACTIVE);
        }
        while (current != null) {
            r.compare();
            if (value == current.value) {
                r.mark(current, TreeHighlight.FOUND);
                r.record(2, "Found " + value + " after " + r.comparisons() + " comparison"
                        + (r.comparisons() == 1 ? "" : "s") + ".", StepStatus.SUCCESS);
                return r.trace();
            }
            boolean goLeft = value < current.value;
            TreeNode next = goLeft ? current.left : current.right;
            String cmp = value + (goLeft ? " is less than " : " is greater than ") + current.value;
            r.mark(current, TreeHighlight.PATH);
            if (next == null) {
                r.record(goLeft ? 3 : 4, cmp + ", but the " + (goLeft ? "left" : "right")
                        + " child is empty.", StepStatus.ACTIVE);
            } else {
                r.mark(next, TreeHighlight.CURRENT);
                r.record(goLeft ? 3 : 4, cmp + ", so we move to the " + (goLeft ? "left" : "right")
                        + " subtree (" + next.value + ").", StepStatus.ACTIVE);
            }
            current = next;
        }
        r.record(5, value + " is not in the tree" + (root == null ? " (the tree is empty)." : "."),
                StepStatus.FAILURE);
        return r.trace();
    }

    public ExecutionTrace<TreeStep> delete(int value) {
        Recorder r = new Recorder(TreeOperation.DELETE);
        r.record(-1, "Delete " + value + ". First find it, exactly like a search.", StepStatus.INFO);

        TreeNode parent = null;
        TreeNode current = root;
        while (current != null) {
            r.mark(current, TreeHighlight.CURRENT);
            r.compare();
            if (value == current.value) {
                break;
            }
            boolean goLeft = value < current.value;
            r.record(0, value + (goLeft ? " is less than " : " is greater than ") + current.value
                    + ", so we move to the " + (goLeft ? "left" : "right") + " subtree.", StepStatus.ACTIVE);
            r.mark(current, TreeHighlight.PATH);
            parent = current;
            current = goLeft ? current.left : current.right;
        }
        if (current == null) {
            r.record(1, value + " is not in the tree, so nothing is deleted.", StepStatus.FAILURE);
            return r.trace();
        }

        TreeNode target = current;
        r.mark(target, TreeHighlight.DELETE_TARGET);
        if (target.isLeaf()) {
            r.record(2, "Found " + value + ". It is a leaf (no children), so it can simply be removed.",
                    StepStatus.WARNING);
            replaceChild(parent, target, null);
            size--;
            r.unmark(target);
            r.record(2, value + " has been removed" + (parent == null ? "; the tree is now empty." :
                    " from " + parent.value + "."), StepStatus.SUCCESS);
        } else if (target.left == null || target.right == null) {
            TreeNode child = target.left != null ? target.left : target.right;
            r.mark(child, TreeHighlight.SUCCESSOR);
            r.record(3, "Found " + value + ". It has one child (" + child.value + "), so "
                    + (parent == null ? "that child becomes the new root." : "its parent " + parent.value
                    + " will point directly to " + child.value + "."), StepStatus.WARNING);
            replaceChild(parent, target, child);
            size--;
            r.unmark(target);
            r.mark(child, TreeHighlight.PATH);
            r.record(3, value + " has been removed and its subtree moved up one level.", StepStatus.SUCCESS);
        } else {
            r.record(4, "Found " + value + ". It has two children, so its inorder successor "
                    + "(the smallest value in its right subtree) is used.", StepStatus.WARNING);
            TreeNode succParent = target;
            TreeNode succ = target.right;
            r.mark(succ, TreeHighlight.CURRENT);
            r.record(5, "Go to the right child " + succ.value + ", then keep going left.", StepStatus.ACTIVE);
            while (succ.left != null) {
                r.mark(succ, TreeHighlight.PATH);
                succParent = succ;
                succ = succ.left;
                r.mark(succ, TreeHighlight.CURRENT);
                r.record(5, "Move left to " + succ.value + ".", StepStatus.ACTIVE);
            }
            r.mark(succ, TreeHighlight.SUCCESSOR);
            r.record(5, succ.value + " has no left child, so it is the inorder successor of " + value + ".",
                    StepStatus.ACTIVE);
            int succValue = succ.value;
            target.value = succValue;
            r.record(6, "Copy " + succValue + " into the node that held " + value
                    + ". The BST property still holds: everything left is smaller, everything right is larger.",
                    StepStatus.WARNING);
            if (succParent == target) {
                succParent.right = succ.right;
            } else {
                succParent.left = succ.right;
            }
            size--;
            r.unmark(succ);
            r.mark(target, TreeHighlight.FOUND);
            r.record(7, "Remove the old successor node " + (succ.right == null ? "(it was a leaf)."
                    : "by linking its parent to its right child " + succ.right.value + "."), StepStatus.SUCCESS);
        }
        return r.trace();
    }

    public ExecutionTrace<TreeStep> traverse(TreeOperation order) {
        if (order == null || !order.isTraversal()) {
            throw new IllegalArgumentException(order + " is not a traversal");
        }
        Recorder r = new Recorder(order);
        if (root == null) {
            r.record(-1, "The tree is empty, so there is nothing to traverse.", StepStatus.FAILURE);
            return r.trace();
        }
        r.record(-1, describe(order), StepStatus.INFO);
        if (order == TreeOperation.LEVEL_ORDER) {
            levelOrder(r);
        } else {
            depthFirst(order, root, r);
        }
        r.record(-1, order.label() + " traversal complete: " + r.output, StepStatus.SUCCESS);
        return r.trace();
    }

    private void depthFirst(TreeOperation order, TreeNode node, Recorder r) {
        if (node == null) {
            return;
        }
        r.mark(node, TreeHighlight.CURRENT);
        String hint = switch (order) {
            case INORDER -> "In inorder, the left subtree is visited before the current node.";
            case PREORDER -> "In preorder, the current node is visited before its subtrees.";
            default -> "In postorder, both subtrees are visited before the current node.";
        };
        r.record(0, "Arrive at " + node.value + ". " + hint, StepStatus.ACTIVE);
        r.mark(node, TreeHighlight.PATH);
        if (order == TreeOperation.PREORDER) {
            visit(node, r, 2);
        }
        depthFirst(order, node.left, r);
        if (order == TreeOperation.INORDER) {
            visit(node, r, 3);
        }
        depthFirst(order, node.right, r);
        if (order == TreeOperation.POSTORDER) {
            visit(node, r, 4);
        }
    }

    private void visit(TreeNode node, Recorder r, int line) {
        r.visit(node);
        r.record(line, "Visit " + node.value + " → output so far: " + r.output, StepStatus.SUCCESS);
    }

    private void levelOrder(Recorder r) {
        ArrayDeque<TreeNode> queue = new ArrayDeque<>();
        queue.add(root);
        r.mark(root, TreeHighlight.PATH);
        r.record(0, "Put the root " + root.value + " in the queue.", StepStatus.INFO);
        while (!queue.isEmpty()) {
            TreeNode n = queue.poll();
            r.visit(n);
            List<String> added = new ArrayList<>();
            for (TreeNode c : new TreeNode[]{n.left, n.right}) {
                if (c != null) {
                    queue.add(c);
                    r.mark(c, TreeHighlight.PATH);
                    added.add(String.valueOf(c.value));
                }
            }
            r.record(2, "Dequeue and visit " + n.value + (added.isEmpty() ? ". It has no children."
                    : ". Enqueue its children " + String.join(" and ", added) + ".")
                    + " Level-order visits the tree one level at a time.", StepStatus.SUCCESS);
        }
    }

    private static String describe(TreeOperation order) {
        return switch (order) {
            case INORDER -> "Inorder: left subtree, node, right subtree. For a BST this outputs the values in sorted order.";
            case PREORDER -> "Preorder: node, left subtree, right subtree. Useful for copying a tree.";
            case POSTORDER -> "Postorder: left subtree, right subtree, node. Useful for deleting a tree bottom-up.";
            default -> "Level-order: visit nodes level by level using a queue (breadth-first).";
        };
    }

    // ---------------------------------------------------------------- helpers

    private TreeNode newNode(int value) {
        size++;
        return new TreeNode(nextId++, value);
    }

    private void replaceChild(TreeNode parent, TreeNode oldChild, TreeNode newChild) {
        if (parent == null) {
            root = newChild;
        } else if (parent.left == oldChild) {
            parent.left = newChild;
        } else {
            parent.right = newChild;
        }
    }

    /** Collects the steps of one operation, snapshotting the live tree each time. */
    private final class Recorder {
        private final TreeOperation operation;
        private final long t0 = System.nanoTime();
        private final List<TreeStep> steps = new ArrayList<>();
        private final Map<Integer, TreeHighlight> highlights = new HashMap<>();
        private final List<Integer> output = new ArrayList<>();
        private final ExecutionMetrics.Counter counter = new ExecutionMetrics.Counter();

        Recorder(TreeOperation operation) {
            this.operation = operation;
            if (operation.isTraversal()) {
                counter.track(MetricType.NODES_VISITED);
            } else if (operation != TreeOperation.VIEW) {
                counter.track(MetricType.COMPARISONS);
            }
        }

        void mark(TreeNode n, TreeHighlight h) {
            highlights.put(n.id, h);
        }

        void unmark(TreeNode n) {
            highlights.remove(n.id);
        }

        void compare() {
            counter.increment(MetricType.COMPARISONS);
        }

        long comparisons() {
            return counter.get(MetricType.COMPARISONS);
        }

        void visit(TreeNode n) {
            output.add(n.value);
            counter.increment(MetricType.NODES_VISITED);
            mark(n, TreeHighlight.VISITED);
        }

        void record(int line, String text, StepStatus status) {
            TreeSnapshot snap = TreeSnapshot.of(root);
            counter.set(MetricType.NODE_COUNT, snap.size());
            counter.set(MetricType.TREE_HEIGHT, snap.height());
            steps.add(new TreeStep(operation, snap, highlights, output, counter.snapshot(), line, text, status));
        }

        ExecutionTrace<TreeStep> trace() {
            return new ExecutionTrace<>(operation.label(), steps, System.nanoTime() - t0);
        }
    }
}
