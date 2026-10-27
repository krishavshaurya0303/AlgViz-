package com.algoviz.engine.linkedlist;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.MetricType;
import com.algoviz.model.StepStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A genuine singly linked list (head pointer only, no tail pointer, no size
 * shortcuts for traversal). Every user operation returns a trace in which each
 * pointer assignment is a separate, visible step. Indices are zero-based.
 * Invalid requests (bad index, empty list) leave the list unchanged and return
 * a trace whose last step has status FAILURE.
 */
public final class SinglyLinkedList {

    private ListNode head;
    private int size;
    private int nextId = 1;

    // ----------------------------------------------------------------- queries

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return head == null;
    }

    /** Values from head to tail by following next pointers. */
    public List<Integer> values() {
        List<Integer> out = new ArrayList<>();
        for (ListNode n = head; n != null; n = n.next) {
            out.add(n.value);
        }
        return out;
    }

    /** Appends without recording (used for sample data). */
    public void appendSilently(int value) {
        ListNode node = new ListNode(nextId++, value);
        if (head == null) {
            head = node;
        } else {
            ListNode c = head;
            while (c.next != null) {
                c = c.next;
            }
            c.next = node;
        }
        size++;
    }

    // ------------------------------------------------------- traced operations

    public ExecutionTrace<ListStep> insertAtHead(int value) {
        Recorder r = new Recorder(ListOperation.INSERT_HEAD);
        r.record(-1, "Insert " + value + " at the head. This needs no traversal.", StepStatus.INFO);
        ListNode node = r.create(value, 0);
        r.record(0, "Create a new node holding " + value + ". Its next pointer starts as null.", StepStatus.ACTIVE);
        node.next = head;
        r.linkChanged(node);
        r.record(1, "Point the new node's next to the current head"
                + (head == null ? " (null, because the list is empty)." : " (" + head.value + ")."), StepStatus.WARNING);
        head = node;
        size++;
        r.headChanged();
        r.record(2, "Move head to the new node. " + value + " is now the first element.", StepStatus.SUCCESS);
        r.settle();
        r.record(-1, "Inserted " + value + " at index 0.", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> insertAtTail(int value) {
        Recorder r = new Recorder(ListOperation.INSERT_TAIL);
        r.record(-1, "Insert " + value + " at the tail. Without a tail pointer we must walk to the last node.",
                StepStatus.INFO);
        ListNode node = r.create(value, r.layoutSize());
        r.record(0, "Create a new node holding " + value + ".", StepStatus.ACTIVE);
        if (head == null) {
            head = node;
            size++;
            r.headChanged();
            r.record(1, "The list is empty, so head simply points to the new node.", StepStatus.SUCCESS);
            r.settle();
            r.record(-1, "Inserted " + value + " as the only element.", StepStatus.SUCCESS);
            return r.trace();
        }
        ListNode current = head;
        r.moveCurrent(current);
        r.record(2, "Start with current at the head (" + current.value + ").", StepStatus.ACTIVE);
        while (current.next != null) {
            current = current.next;
            r.moveCurrent(current);
            r.record(3, "current.next is not null, so advance current to " + current.value + ".", StepStatus.ACTIVE);
        }
        current.next = node;
        size++;
        r.linkChanged(current);
        r.record(4, current.value + " is the last node. Point its next to the new node " + value + ".",
                StepStatus.WARNING);
        r.settle();
        r.record(-1, "Inserted " + value + " at index " + (size - 1) + ".", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> insertAt(int index, int value) {
        if (index == 0) {
            return insertAtHead(value);
        }
        Recorder r = new Recorder(ListOperation.INSERT_AT);
        if (index < 0 || index > size) {
            r.record(-1, "Index " + index + " is out of range. Valid insert positions are 0.." + size + ".",
                    StepStatus.FAILURE);
            return r.trace();
        }
        r.record(-1, "Insert " + value + " at index " + index + ". Walk to the node just before that position.",
                StepStatus.INFO);
        ListNode current = head;
        r.moveCurrent(current);
        r.record(1, "Start with current at the head (" + current.value + ", index 0).", StepStatus.ACTIVE);
        for (int i = 1; i < index; i++) {
            current = current.next;
            r.moveCurrent(current);
            r.record(2, "Advance current to " + current.value + " (index " + i + ").", StepStatus.ACTIVE);
        }
        ListNode node = r.create(value, index);
        r.record(3, "Create a new node holding " + value + ".", StepStatus.ACTIVE);
        node.next = current.next;
        r.linkChanged(node);
        r.record(4, "First point the new node's next to " + (node.next == null ? "null" : node.next.value)
                + " so the rest of the list is not lost.", StepStatus.WARNING);
        current.next = node;
        size++;
        r.linkChanged(current);
        r.record(5, "Then redirect " + current.value + ".next to the new node.", StepStatus.WARNING);
        r.settle();
        r.record(-1, "Inserted " + value + " at index " + index + ".", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> deleteHead() {
        Recorder r = new Recorder(ListOperation.DELETE_HEAD);
        if (head == null) {
            r.record(0, "The list is empty — there is nothing to delete.", StepStatus.FAILURE);
            return r.trace();
        }
        ListNode removed = head;
        r.highlight(removed, ListHighlight.REMOVING);
        r.pointer("removed", removed);
        r.record(1, "removed = head (" + removed.value + ").", StepStatus.WARNING);
        head = head.next;
        size--;
        r.headChanged();
        r.detach(removed);
        r.record(2, "Move head to " + (head == null ? "null" : head.value)
                + ". Nothing points to " + removed.value + " any more.", StepStatus.WARNING);
        r.remove(removed);
        r.settle();
        r.record(-1, "Deleted " + removed.value + " from the head.", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> deleteTail() {
        Recorder r = new Recorder(ListOperation.DELETE_TAIL);
        if (head == null) {
            r.record(0, "The list is empty — there is nothing to delete.", StepStatus.FAILURE);
            return r.trace();
        }
        if (head.next == null) {
            ListNode removed = head;
            r.highlight(removed, ListHighlight.REMOVING);
            r.record(1, "The list has a single node (" + removed.value + "), which is also the tail.",
                    StepStatus.WARNING);
            head = null;
            size--;
            r.headChanged();
            r.detach(removed);
            r.record(1, "Set head to null. The list is now empty.", StepStatus.WARNING);
            r.remove(removed);
            r.settle();
            r.record(-1, "Deleted " + removed.value + ".", StepStatus.SUCCESS);
            return r.trace();
        }
        ListNode current = head;
        r.moveCurrent(current);
        r.record(2, "Start with current at the head (" + current.value + ").", StepStatus.ACTIVE);
        while (current.next.next != null) {
            current = current.next;
            r.moveCurrent(current);
            r.record(3, "current.next.next is not null, so advance current to " + current.value + ".",
                    StepStatus.ACTIVE);
        }
        ListNode removed = current.next;
        r.highlight(removed, ListHighlight.REMOVING);
        r.record(3, current.value + " is the second-to-last node; " + removed.value + " is the tail.",
                StepStatus.ACTIVE);
        current.next = null;
        size--;
        r.linkChanged(current);
        r.detach(removed);
        r.record(4, "Set " + current.value + ".next to null. " + current.value + " is the new tail.",
                StepStatus.WARNING);
        r.remove(removed);
        r.settle();
        r.record(-1, "Deleted " + removed.value + " from the tail.", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> deleteAt(int index) {
        if (index == 0 && head != null) {
            return deleteHead();
        }
        Recorder r = new Recorder(ListOperation.DELETE_AT);
        if (head == null) {
            r.record(-1, "The list is empty — there is nothing to delete.", StepStatus.FAILURE);
            return r.trace();
        }
        if (index < 0 || index >= size) {
            r.record(-1, "Index " + index + " is out of range. Valid indices are 0.." + (size - 1) + ".",
                    StepStatus.FAILURE);
            return r.trace();
        }
        r.record(-1, "Delete the node at index " + index + ". Walk to the node just before it.", StepStatus.INFO);
        ListNode current = head;
        r.moveCurrent(current);
        r.record(1, "Start with current at the head (" + current.value + ", index 0).", StepStatus.ACTIVE);
        for (int i = 1; i < index; i++) {
            current = current.next;
            r.moveCurrent(current);
            r.record(2, "Advance current to " + current.value + " (index " + i + ").", StepStatus.ACTIVE);
        }
        ListNode removed = current.next;
        r.highlight(removed, ListHighlight.REMOVING);
        r.pointer("removed", removed);
        r.record(3, "removed = current.next (" + removed.value + ").", StepStatus.ACTIVE);
        current.next = removed.next;
        size--;
        r.linkChanged(current);
        r.detach(removed);
        r.record(4, "Bypass " + removed.value + ": point " + current.value + ".next to "
                + (current.next == null ? "null" : current.next.value) + ".", StepStatus.WARNING);
        r.remove(removed);
        r.settle();
        r.record(-1, "Deleted " + removed.value + " from index " + index + ".", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> search(int value) {
        Recorder r = new Recorder(ListOperation.SEARCH);
        r.track(MetricType.COMPARISONS);
        r.record(-1, "Search for " + value + " by following next pointers from the head.", StepStatus.INFO);
        ListNode current = head;
        int index = 0;
        if (current == null) {
            r.pointer("current", null);
            r.record(0, "head is null — the list is empty.", StepStatus.ACTIVE);
        } else {
            r.moveCurrent(current);
            r.record(0, "Start with current at the head.", StepStatus.ACTIVE);
        }
        while (current != null) {
            r.compare();
            if (current.value == value) {
                r.highlight(current, ListHighlight.FOUND);
                r.record(2, current.value + " equals " + value + ". Found at index " + index + ".", StepStatus.SUCCESS);
                return r.trace();
            }
            r.record(2, current.value + " is not " + value + ".", StepStatus.ACTIVE);
            current = current.next;
            index++;
            if (current != null) {
                r.moveCurrent(current);
                r.record(3, "Advance current to the next node (" + current.value + ", index " + index + ").",
                        StepStatus.ACTIVE);
            } else {
                r.moveCurrent(null);
                r.record(3, "Advance current: it is now null — we have passed the last node.", StepStatus.ACTIVE);
            }
        }
        r.record(4, value + " is not in the list.", StepStatus.FAILURE);
        return r.trace();
    }

    public ExecutionTrace<ListStep> traverse() {
        Recorder r = new Recorder(ListOperation.TRAVERSE);
        if (head == null) {
            r.record(0, "head is null — the list is empty, so there is nothing to traverse.", StepStatus.FAILURE);
            return r.trace();
        }
        r.record(-1, "Traverse the list from head to tail.", StepStatus.INFO);
        List<Integer> seen = new ArrayList<>();
        ListNode current = head;
        r.moveCurrent(current);
        r.record(0, "current = head (" + current.value + ").", StepStatus.ACTIVE);
        while (current != null) {
            seen.add(current.value);
            r.record(2, "Visit " + current.value + ". Visited so far: " + seen + ".", StepStatus.SUCCESS);
            current = current.next;
            r.moveCurrent(current);
            r.record(3, current == null ? "current.next is null — the end of the list has been reached."
                    : "Follow the next pointer to " + current.value + ".", StepStatus.ACTIVE);
        }
        r.record(-1, "Traversal complete: " + seen + ".", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> reverse() {
        Recorder r = new Recorder(ListOperation.REVERSE);
        if (head == null) {
            r.record(0, "The list is empty; reversing it changes nothing.", StepStatus.FAILURE);
            return r.trace();
        }
        r.record(-1, "Reverse the list in place by redirecting every next pointer.", StepStatus.INFO);
        ListNode prev = null;
        ListNode current = head;
        r.pointer("prev", null);
        r.moveCurrent(current);
        r.record(0, "prev = null, current = head (" + current.value + ").", StepStatus.ACTIVE);
        while (current != null) {
            ListNode next = current.next;
            r.pointer("next", next);
            r.record(2, "Save next = " + (next == null ? "null" : String.valueOf(next.value))
                    + " so we do not lose the rest of the list.", StepStatus.ACTIVE);
            current.next = prev;
            r.linkChanged(current);
            r.record(3, "Redirect " + current.value + ".next to " + (prev == null ? "null" : String.valueOf(prev.value))
                    + " — this arrow now points backwards.", StepStatus.WARNING);
            prev = current;
            r.pointer("prev", prev);
            r.record(4, "Advance prev to " + prev.value + ".", StepStatus.ACTIVE);
            current = next;
            r.moveCurrent(current);
            r.record(5, "Advance current to " + (current == null ? "null" : String.valueOf(current.value)) + ".",
                    StepStatus.ACTIVE);
        }
        head = prev;
        r.headChanged();
        r.clearPointer("next");
        r.record(6, "current is null, so every pointer is reversed. Set head = prev (" + prev.value + ").",
                StepStatus.WARNING);
        r.clearPointer("prev");
        r.clearPointer("current");
        r.settle();
        r.record(-1, "The list is reversed: " + values() + ".", StepStatus.SUCCESS);
        return r.trace();
    }

    public ExecutionTrace<ListStep> clear() {
        Recorder r = new Recorder(ListOperation.CLEAR);
        r.record(-1, "Clear the list.", StepStatus.INFO);
        head = null;
        size = 0;
        r.headChanged();
        r.removeAll();
        r.record(0, "head = null. The nodes are unreachable and Java's garbage collector reclaims them.",
                StepStatus.SUCCESS);
        return r.trace();
    }

    // ---------------------------------------------------------------- recorder

    /** Records the real pointer structure; display order is kept stable within one operation. */
    private final class Recorder {
        private final ListOperation operation;
        private final long t0 = System.nanoTime();
        private final List<ListStep> steps = new ArrayList<>();
        private final List<ListNode> layout = new ArrayList<>();
        private final Set<Integer> detached = new HashSet<>();
        private final Map<String, ListNode> pointers = new LinkedHashMap<>();
        private final Map<Integer, ListHighlight> highlights = new HashMap<>();
        private final ExecutionMetrics.Counter counter = new ExecutionMetrics.Counter()
                .track(MetricType.NODES_VISITED, MetricType.POINTER_CHANGES);
        private int changedLink = -1;
        private boolean headChanged;

        Recorder(ListOperation operation) {
            this.operation = operation;
            for (ListNode n = head; n != null; n = n.next) {
                layout.add(n);
            }
        }

        void track(MetricType type) {
            counter.track(type);
        }

        int layoutSize() {
            return layout.size();
        }

        ListNode create(int value, int position) {
            ListNode node = new ListNode(nextId++, value);
            layout.add(Math.min(position, layout.size()), node);
            detached.add(node.id);
            highlights.put(node.id, ListHighlight.NEW);
            pointers.put("new", node);
            return node;
        }

        void moveCurrent(ListNode node) {
            ListNode old = pointers.get("current");
            if (old != null && highlights.get(old.id) == ListHighlight.CURRENT) {
                highlights.put(old.id, ListHighlight.VISITED);
            }
            pointers.put("current", node);
            if (node != null) {
                highlights.put(node.id, ListHighlight.CURRENT);
                counter.increment(MetricType.NODES_VISITED);
            }
        }

        void pointer(String name, ListNode node) {
            pointers.put(name, node);
        }

        void clearPointer(String name) {
            pointers.remove(name);
        }

        void highlight(ListNode node, ListHighlight h) {
            highlights.put(node.id, h);
        }

        void compare() {
            counter.increment(MetricType.COMPARISONS);
        }

        void linkChanged(ListNode node) {
            changedLink = node.id;
            counter.increment(MetricType.POINTER_CHANGES);
        }

        void headChanged() {
            headChanged = true;
            counter.increment(MetricType.POINTER_CHANGES);
        }

        void detach(ListNode node) {
            detached.add(node.id);
        }

        void remove(ListNode node) {
            layout.remove(node);
            detached.remove(node.id);
            highlights.remove(node.id);
            pointers.values().removeIf(p -> p == node);
        }

        void removeAll() {
            layout.clear();
            detached.clear();
            highlights.clear();
            pointers.clear();
        }

        /** Final layout: actual list order, nothing detached, helper pointers removed. */
        void settle() {
            layout.clear();
            for (ListNode n = head; n != null; n = n.next) {
                layout.add(n);
            }
            detached.clear();
            pointers.clear();
            highlights.values().removeIf(h -> h != ListHighlight.NEW);
        }

        void record(int line, String text, StepStatus status) {
            List<ListSnapshot.NodeView> views = new ArrayList<>();
            for (ListNode n : layout) {
                views.add(new ListSnapshot.NodeView(n.id, n.value, n.next == null ? -1 : n.next.id,
                        detached.contains(n.id)));
            }
            Map<String, Integer> ptrs = new LinkedHashMap<>();
            pointers.forEach((k, v) -> ptrs.put(k, v == null ? -1 : v.id));
            counter.set(MetricType.NODE_COUNT, size);
            ListSnapshot snap = new ListSnapshot(views, head == null ? -1 : head.id, ptrs, highlights,
                    changedLink, headChanged);
            steps.add(new ListStep(operation, snap, counter.snapshot(), line, text, status));
            changedLink = -1;
            headChanged = false;
        }

        ExecutionTrace<ListStep> trace() {
            return new ExecutionTrace<>(operation.label(), steps, System.nanoTime() - t0);
        }
    }
}
