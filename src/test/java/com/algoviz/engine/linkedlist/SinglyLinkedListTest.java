package com.algoviz.engine.linkedlist;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.MetricType;
import com.algoviz.model.StepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinglyLinkedListTest {

    private SinglyLinkedList list;

    @BeforeEach
    void setUp() {
        list = new SinglyLinkedList();
        list.insertAtTail(10);
        list.insertAtTail(20);
        list.insertAtTail(30);
    }

    /** Follows next pointers in the final snapshot and checks it matches values(). */
    private static void assertLinksConsistent(ExecutionTrace<ListStep> t, List<Integer> expected) {
        ListSnapshot s = t.last().snapshot();
        Set<Integer> seen = new HashSet<>();
        int id = s.headId();
        List<Integer> walked = new java.util.ArrayList<>();
        while (id != -1) {
            assertTrue(seen.add(id), "cycle in list");
            int idx = s.indexOf(id);
            ListSnapshot.NodeView n = s.nodes().get(idx);
            walked.add(n.value());
            id = n.nextId();
        }
        assertEquals(expected, walked);
        assertEquals(expected.size(), s.nodes().size());
        // final layout is the real list order
        assertEquals(expected, s.nodes().stream().map(ListSnapshot.NodeView::value).toList());
    }

    @Test
    void insertAtAllPositions() {
        assertLinksConsistent(list.insertAtHead(5), List.of(5, 10, 20, 30));
        assertLinksConsistent(list.insertAt(2, 15), List.of(5, 10, 15, 20, 30));
        assertLinksConsistent(list.insertAt(5, 40), List.of(5, 10, 15, 20, 30, 40));
        assertLinksConsistent(list.insertAtTail(50), List.of(5, 10, 15, 20, 30, 40, 50));
        assertEquals(7, list.size());
    }

    @Test
    void deleteAtAllPositions() {
        list.insertAtTail(40);
        assertLinksConsistent(list.deleteHead(), List.of(20, 30, 40));
        assertLinksConsistent(list.deleteTail(), List.of(20, 30));
        list.insertAtTail(35);
        assertLinksConsistent(list.deleteAt(1), List.of(20, 35));
        assertLinksConsistent(list.deleteAt(0), List.of(35));
        assertEquals(1, list.size());
    }

    @Test
    void emptyListBehaviour() {
        SinglyLinkedList empty = new SinglyLinkedList();
        assertEquals(StepStatus.FAILURE, empty.deleteHead().last().status());
        assertEquals(StepStatus.FAILURE, empty.deleteTail().last().status());
        assertEquals(StepStatus.FAILURE, empty.deleteAt(0).last().status());
        assertEquals(StepStatus.FAILURE, empty.search(1).last().status());
        assertEquals(StepStatus.FAILURE, empty.traverse().last().status());
        assertEquals(StepStatus.FAILURE, empty.reverse().last().status());
        assertLinksConsistent(empty.insertAtTail(7), List.of(7));
    }

    @Test
    void singleNodeList() {
        SinglyLinkedList one = new SinglyLinkedList();
        one.insertAtHead(1);
        assertLinksConsistent(one.reverse(), List.of(1));
        assertLinksConsistent(one.deleteTail(), List.of());
        assertTrue(one.isEmpty());
        one.insertAtHead(2);
        assertLinksConsistent(one.deleteAt(0), List.of());
    }

    @Test
    void invalidPositionsLeaveListUnchanged() {
        assertEquals(StepStatus.FAILURE, list.insertAt(4, 99).last().status());
        assertEquals(StepStatus.FAILURE, list.insertAt(-1, 99).last().status());
        assertEquals(StepStatus.FAILURE, list.deleteAt(3).last().status());
        assertEquals(StepStatus.FAILURE, list.deleteAt(-2).last().status());
        assertEquals(List.of(10, 20, 30), list.values());
    }

    @Test
    void search() {
        ExecutionTrace<ListStep> found = list.search(20);
        assertEquals(StepStatus.SUCCESS, found.last().status());
        assertEquals(2, found.last().metrics().get(MetricType.COMPARISONS));
        ExecutionTrace<ListStep> missing = list.search(99);
        assertEquals(StepStatus.FAILURE, missing.last().status());
        assertEquals(3, missing.last().metrics().get(MetricType.COMPARISONS));
    }

    @Test
    void traverseVisitsEveryNode() {
        ExecutionTrace<ListStep> t = list.traverse();
        assertEquals(3, t.last().metrics().get(MetricType.NODES_VISITED));
    }

    @Test
    void reverseRedirectsEveryPointer() {
        ExecutionTrace<ListStep> t = list.reverse();
        assertLinksConsistent(t, List.of(30, 20, 10));
        // 3 next-pointer redirects + 1 head change
        assertEquals(4, t.last().metrics().get(MetricType.POINTER_CHANGES));
        // Mid-operation, layout is kept in the original order while arrows flip.
        ListStep mid = t.steps().stream()
                .filter(s -> s.snapshot().changedLink() != -1).skip(1).findFirst().orElseThrow();
        ListSnapshot s = mid.snapshot();
        assertEquals(List.of(10, 20, 30), s.nodes().stream().map(ListSnapshot.NodeView::value).toList());
        assertEquals(s.nodes().get(0).id(), s.nodes().get(1).nextId(), "20 now points back to 10");
        assertEquals(List.of(30, 20, 10), list.values());
    }

    @Test
    void insertLinksNewNodeBeforeRedirecting() {
        ExecutionTrace<ListStep> t = list.insertAt(1, 15);
        // find the step where the new node's next was set: the old link 10->20 must still exist
        ListStep linkNew = t.steps().stream().filter(s -> s.pseudocodeLine() == 4).findFirst().orElseThrow();
        ListSnapshot s = linkNew.snapshot();
        ListSnapshot.NodeView ten = s.nodes().get(s.indexOf(s.headId()));
        ListSnapshot.NodeView fresh = s.nodes().stream().filter(ListSnapshot.NodeView::detached).findFirst().orElseThrow();
        int twentyId = s.nodes().stream().filter(n -> n.value() == 20).findFirst().orElseThrow().id();
        assertEquals(twentyId, ten.nextId());
        assertEquals(twentyId, fresh.nextId());
    }

    @Test
    void clearEmptiesTheList() {
        assertLinksConsistent(list.clear(), List.of());
        assertEquals(0, list.size());
    }
}
