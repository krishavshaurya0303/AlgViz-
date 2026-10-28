package com.algoviz.engine.memory;

import com.algoviz.engine.linkedlist.ListSnapshot;
import com.algoviz.engine.tree.TreeSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * What the stack and the heap hold at one step, worked out from a linked-list or
 * tree snapshot (and the snapshot before it, so changes can be marked).
 * <p>
 * Stack: the variables of the running operation ({@code head}, {@code prev},
 * {@code current}, {@code root} ...), each holding an address or null.
 * Heap: one block per node, in address order, with its fields.
 * A node that exists now but did not in the previous step was just allocated.
 * A node that existed before but is gone was freed (C) or became garbage (Java);
 * in Java it stays on the heap, greyed out, for the rest of the operation.
 *
 * @param stack variables in display order
 * @param heap  blocks in address order
 * @param note  one sentence about what happened in memory at this step, or ""
 */
public record MemoryLayout(List<Variable> stack, List<Block> heap, String note) {

    /** Bytes per block: a 16-byte C list node / 24-byte tree node plus a 16-byte allocator header. */
    static final int LIST_STRIDE = 0x20;
    static final int TREE_STRIDE = 0x30;

    public enum BlockState { LIVE, NEW, FREED, GARBAGE }

    public record Variable(String name, String value, boolean changed) {
    }

    public record Field(String name, String value, boolean changed) {
    }

    public record Block(int id, String address, String type, List<Field> fields, BlockState state) {
    }

    public MemoryLayout {
        stack = List.copyOf(stack);
        heap = List.copyOf(heap);
    }

    // ------------------------------------------------------------ linked list

    /**
     * @param seen nodes (id → value) that existed at any earlier step of this operation;
     *             used to keep Java garbage visible
     */
    public static MemoryLayout ofList(ListSnapshot cur, ListSnapshot prev, Map<Integer, Integer> seen, MemoryModel m) {
        Function<Integer, String> ref = id -> id == -1 ? m.nullText() : m.address(id, LIST_STRIDE);

        List<Variable> stack = new ArrayList<>();
        stack.add(new Variable("head", ref.apply(cur.headId()), prev != null && prev.headId() != cur.headId()));
        cur.pointers().forEach((name, id) -> {
            Integer before = prev == null ? null : prev.pointers().get(name);
            stack.add(new Variable(name, ref.apply(id), prev != null && !id.equals(before)));
        });

        Map<Integer, ListSnapshot.NodeView> before = new HashMap<>();
        if (prev != null) {
            prev.nodes().forEach(n -> before.put(n.id(), n));
        }
        List<Block> heap = new ArrayList<>();
        String note = "";
        for (ListSnapshot.NodeView n : cur.nodes()) {
            ListSnapshot.NodeView old = before.get(n.id());
            boolean isNew = prev != null && old == null;
            heap.add(new Block(n.id(), m.address(n.id(), LIST_STRIDE), m.typeName(), List.of(
                    new Field("data", Integer.toString(n.value()), false),
                    new Field("next", ref.apply(n.nextId()), old != null && old.nextId() != n.nextId())),
                    isNew ? BlockState.NEW : BlockState.LIVE));
            if (isNew) {
                note = allocated(m, n.value(), m.address(n.id(), LIST_STRIDE), 16);
            }
        }
        note = addGone(heap, cur.nodes().stream().map(ListSnapshot.NodeView::id).toList(), before.keySet(),
                id -> before.get(id).value(), seen, m, LIST_STRIDE, 16, List.of("data", "next"), note);
        return new MemoryLayout(stack, sorted(heap), note);
    }

    // -------------------------------------------------------------------- tree

    /**
     * @param vars     stack variables (e.g. root, current, successor) → node id or -1
     * @param prevVars the same for the previous step, or null
     * @param seen     nodes (id → value) that existed at any earlier step of this operation
     */
    public static MemoryLayout ofTree(TreeSnapshot cur, TreeSnapshot prev, Map<String, Integer> vars,
                                      Map<String, Integer> prevVars, Map<Integer, Integer> seen, MemoryModel m) {
        Function<Integer, String> ref = id -> id == -1 ? m.nullText() : m.address(id, TREE_STRIDE);

        List<Variable> stack = new ArrayList<>();
        vars.forEach((name, id) -> stack.add(new Variable(name, ref.apply(id),
                prevVars != null && !id.equals(prevVars.getOrDefault(name, -1)))));

        Map<Integer, TreeSnapshot.Node> now = index(cur);
        Map<Integer, TreeSnapshot.Node> before = prev == null ? Map.of() : index(prev);
        List<Block> heap = new ArrayList<>();
        String note = "";
        for (TreeSnapshot.Node n : now.values()) {
            TreeSnapshot.Node old = before.get(n.id());
            boolean isNew = prev != null && old == null;
            int left = idOf(n.left());
            int right = idOf(n.right());
            heap.add(new Block(n.id(), m.address(n.id(), TREE_STRIDE), m.typeName(), List.of(
                    new Field("data", Integer.toString(n.value()), old != null && old.value() != n.value()),
                    new Field("left", ref.apply(left), old != null && idOf(old.left()) != left),
                    new Field("right", ref.apply(right), old != null && idOf(old.right()) != right)),
                    isNew ? BlockState.NEW : BlockState.LIVE));
            if (isNew) {
                note = allocated(m, n.value(), m.address(n.id(), TREE_STRIDE), 24);
            }
        }
        note = addGone(heap, List.copyOf(now.keySet()), before.keySet(), id -> before.get(id).value(), seen, m,
                TREE_STRIDE, 24, List.of("data", "left", "right"), note);
        return new MemoryLayout(stack, sorted(heap), note);
    }

    // ----------------------------------------------------------------- helpers

    /** Adds freed (C, this step only) or garbage (Java, until the operation ends) blocks. */
    private static String addGone(List<Block> heap, List<Integer> nowIds, java.util.Set<Integer> beforeIds,
                                  Function<Integer, Integer> valueBefore, Map<Integer, Integer> seen, MemoryModel m,
                                  int stride, int bytes, List<String> fieldNames, String note) {
        Map<Integer, Integer> gone = new LinkedHashMap<>();
        if (m == MemoryModel.JAVA) {
            seen.forEach((id, v) -> {
                if (!nowIds.contains(id)) {
                    gone.put(id, v);
                }
            });
        }
        for (Integer id : beforeIds) {
            if (!nowIds.contains(id)) {
                gone.put(id, valueBefore.apply(id));
                String addr = m.address(id, stride);
                note = m == MemoryModel.C
                        ? "free(" + addr + "): the " + bytes + "-byte block holding " + valueBefore.apply(id)
                          + " goes back to the allocator and may be reused by the next malloc."
                        : "The node holding " + valueBefore.apply(id) + " is no longer reachable from any variable, "
                          + "so it is garbage. The garbage collector will reclaim it later; Java has no free().";
            }
        }
        gone.forEach((id, v) -> {
            List<Field> fields = new ArrayList<>();
            fields.add(new Field("data", Integer.toString(v), false));
            for (String f : fieldNames.subList(1, fieldNames.size())) {
                fields.add(new Field(f, m == MemoryModel.C ? "?" : "…", false));
            }
            heap.add(new Block(id, m.address(id, stride), m.typeName(), fields,
                    m == MemoryModel.C ? BlockState.FREED : BlockState.GARBAGE));
        });
        return note;
    }

    private static String allocated(MemoryModel m, int value, String addr, int bytes) {
        return m == MemoryModel.C
                ? "malloc(sizeof(struct node)) reserved " + bytes + " bytes on the heap at " + addr
                  + "; the node's fields are then set (data = " + value + ")."
                : "new Node(" + value + ") created an object on the heap at " + addr
                  + "; the variable holds a reference to it, not the object itself.";
    }

    private static List<Block> sorted(List<Block> heap) {
        heap.sort(Comparator.comparingInt(Block::id));
        return heap;
    }

    private static Map<Integer, TreeSnapshot.Node> index(TreeSnapshot t) {
        Map<Integer, TreeSnapshot.Node> out = new LinkedHashMap<>();
        for (TreeSnapshot.Node n : t.inorder()) {
            out.put(n.id(), n);
        }
        return out;
    }

    private static int idOf(TreeSnapshot.Node n) {
        return n == null ? -1 : n.id();
    }
}
