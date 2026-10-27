package com.algoviz.engine.tree;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.MetricType;
import com.algoviz.model.StepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BinarySearchTreeTest {

    private BinarySearchTree tree;

    /**
     * <pre>
     *         50
     *       /    \
     *     30      70
     *    /  \    /  \
     *   20  40  60  80
     *          \
     *          45
     * </pre>
     */
    @BeforeEach
    void setUp() {
        tree = new BinarySearchTree();
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80, 45}) {
            assertEquals(StepStatus.SUCCESS, tree.insert(v).last().status());
        }
    }

    @Test
    void insertAndSearch() {
        assertEquals(8, tree.size());
        assertTrue(tree.isValidBst());
        ExecutionTrace<TreeStep> found = tree.search(45);
        assertEquals(StepStatus.SUCCESS, found.last().status());
        assertEquals(4, found.last().metrics().get(MetricType.COMPARISONS)); // 50, 30, 40, 45
        ExecutionTrace<TreeStep> missing = tree.search(65);
        assertEquals(StepStatus.FAILURE, missing.last().status());
        assertFalse(tree.contains(65));
    }

    @Test
    void duplicatesAreRejected() {
        ExecutionTrace<TreeStep> t = tree.insert(40);
        assertEquals(StepStatus.FAILURE, t.last().status());
        assertEquals(8, tree.size());
    }

    @Test
    void insertTraceShowsTreeBeforeAndAfter() {
        ExecutionTrace<TreeStep> t = tree.insert(65);
        assertEquals(8, t.first().tree().size());
        assertEquals(9, t.last().tree().size());
        assertEquals(TreeHighlight.INSERTED, t.last().highlights().values().stream()
                .filter(h -> h == TreeHighlight.INSERTED).findFirst().orElseThrow());
    }

    @Test
    void deleteLeaf() {
        tree.delete(20);
        assertEquals(List.of(30, 40, 45, 50, 60, 70, 80), tree.inorderValues());
        assertTrue(tree.isValidBst());
    }

    @Test
    void deleteNodeWithOneChild() {
        tree.delete(40); // has right child 45
        assertEquals(List.of(20, 30, 45, 50, 60, 70, 80), tree.inorderValues());
        assertTrue(tree.isValidBst());
        assertEquals(List.of(50, 30, 20, 45, 70, 60, 80), values(tree.traverse(TreeOperation.PREORDER)));
    }

    @Test
    void deleteNodeWithTwoChildrenUsesInorderSuccessor() {
        ExecutionTrace<TreeStep> t = tree.delete(30); // successor is 40
        assertTrue(t.steps().stream().anyMatch(s -> s.highlights().containsValue(TreeHighlight.SUCCESSOR)));
        assertEquals(List.of(20, 40, 45, 50, 60, 70, 80), tree.inorderValues());
        assertEquals(List.of(50, 40, 20, 45, 70, 60, 80), values(tree.traverse(TreeOperation.PREORDER)));
        assertTrue(tree.isValidBst());
    }

    @Test
    void deleteRootWithTwoChildren() {
        tree.delete(50); // successor 60
        assertEquals(List.of(60, 30, 20, 40, 45, 70, 80), values(tree.traverse(TreeOperation.PREORDER)));
        assertTrue(tree.isValidBst());
    }

    @Test
    void deleteMissingValueChangesNothing() {
        ExecutionTrace<TreeStep> t = tree.delete(99);
        assertEquals(StepStatus.FAILURE, t.last().status());
        assertEquals(8, tree.size());
    }

    @Test
    void traversals() {
        assertEquals(List.of(20, 30, 40, 45, 50, 60, 70, 80), values(tree.traverse(TreeOperation.INORDER)));
        assertEquals(List.of(50, 30, 20, 40, 45, 70, 60, 80), values(tree.traverse(TreeOperation.PREORDER)));
        assertEquals(List.of(20, 45, 40, 30, 60, 80, 70, 50), values(tree.traverse(TreeOperation.POSTORDER)));
        assertEquals(List.of(50, 30, 70, 20, 40, 60, 80, 45), values(tree.traverse(TreeOperation.LEVEL_ORDER)));
        assertEquals(8, tree.traverse(TreeOperation.INORDER).last().metrics().get(MetricType.NODES_VISITED));
    }

    @Test
    void emptyTreeOperations() {
        BinarySearchTree empty = new BinarySearchTree();
        assertEquals(StepStatus.FAILURE, empty.search(1).last().status());
        assertEquals(StepStatus.FAILURE, empty.delete(1).last().status());
        assertEquals(StepStatus.FAILURE, empty.traverse(TreeOperation.INORDER).last().status());
        assertEquals(-1, empty.height());
        assertThrows(IllegalArgumentException.class, () -> empty.traverse(TreeOperation.INSERT));
    }

    @Test
    void heightAndDegenerateTree() {
        assertEquals(3, tree.height());
        BinarySearchTree chain = new BinarySearchTree();
        for (int i = 1; i <= 6; i++) {
            chain.insert(i);
        }
        assertEquals(5, chain.height()); // no automatic balancing
    }

    @Test
    void randomOperationsPreserveBstProperty() {
        Random rnd = new Random(42);
        BinarySearchTree t = new BinarySearchTree();
        TreeSet<Integer> reference = new TreeSet<>();
        for (int k = 0; k < 2000; k++) {
            int v = rnd.nextInt(80);
            if (rnd.nextBoolean()) {
                t.insert(v);
                reference.add(v);
            } else {
                t.delete(v);
                reference.remove(v);
            }
            assertTrue(t.isValidBst());
            assertEquals(reference.size(), t.size());
        }
        assertEquals(new ArrayList<>(reference), t.inorderValues());
        List<Integer> inorder = values(t.traverse(TreeOperation.INORDER));
        List<Integer> sorted = new ArrayList<>(inorder);
        Collections.sort(sorted);
        assertEquals(sorted, inorder);
    }

    @Test
    void nodeIdsAreStableAcrossSnapshots() {
        ExecutionTrace<TreeStep> t = tree.delete(30);
        int rootId = t.first().tree().root().id();
        assertEquals(rootId, t.last().tree().root().id());
    }

    private static List<Integer> values(ExecutionTrace<TreeStep> t) {
        return t.last().output();
    }
}
