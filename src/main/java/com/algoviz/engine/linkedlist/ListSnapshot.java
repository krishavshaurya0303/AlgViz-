package com.algoviz.engine.linkedlist;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable picture of the list's real pointer structure at one step.
 *
 * @param nodes        nodes in display order (left to right). During an operation the
 *                     order is kept stable so that pointer changes are visible — e.g. during
 *                     reversal the arrows flip direction while the boxes stay in place.
 * @param headId       id of the node head points to, or -1 for null
 * @param pointers     named helper pointers (prev, current, next, ...) → node id, -1 for null
 * @param highlights   per-node emphasis
 * @param changedLink  id of the node whose next pointer changed in this step, -1 if none
 * @param headChanged  true if the head pointer changed in this step
 */
public record ListSnapshot(List<NodeView> nodes, int headId, Map<String, Integer> pointers,
                           Map<Integer, ListHighlight> highlights, int changedLink, boolean headChanged) {

    /** One node: its value and the id its next pointer refers to (-1 for null). */
    public record NodeView(int id, int value, int nextId, boolean detached) {
    }

    public ListSnapshot {
        nodes = List.copyOf(nodes);
        pointers = Collections.unmodifiableMap(new LinkedHashMap<>(pointers));
        highlights = Map.copyOf(highlights);
    }

    public int indexOf(int id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).id() == id) {
                return i;
            }
        }
        return -1;
    }
}
