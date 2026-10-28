package com.algoviz.engine.memory;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.linkedlist.ListSnapshot;
import com.algoviz.engine.memory.MemoryLayout.Block;
import com.algoviz.engine.memory.MemoryLayout.BlockState;
import com.algoviz.engine.tree.BinarySearchTree;
import com.algoviz.engine.tree.TreeStep;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemoryLayoutTest {

    private static ListSnapshot list(int headId, Map<String, Integer> pointers, ListSnapshot.NodeView... nodes) {
        return new ListSnapshot(List.of(nodes), headId, pointers, Map.of(), -1, false);
    }

    private static Block block(MemoryLayout l, int id) {
        return l.heap().stream().filter(b -> b.id() == id).findFirst().orElseThrow();
    }

    @Test
    void addressesAreStableAndDistinct() {
        assertEquals("0x1a40", MemoryModel.C.address(1, MemoryLayout.LIST_STRIDE));
        assertEquals("0x1a60", MemoryModel.C.address(2, MemoryLayout.LIST_STRIDE));
        assertEquals("@1a60", MemoryModel.JAVA.address(2, MemoryLayout.LIST_STRIDE));
    }

    @Test
    void newListNodeIsMarkedAllocatedAndLinksShowAddresses() {
        ListSnapshot before = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, -1, false));
        ListSnapshot after = list(1, Map.of("new", 2), new ListSnapshot.NodeView(1, 10, -1, false),
                new ListSnapshot.NodeView(2, 20, -1, true));
        MemoryLayout c = MemoryLayout.ofList(after, before, Map.of(1, 10), MemoryModel.C);
        assertEquals(BlockState.NEW, block(c, 2).state());
        assertEquals("NULL", block(c, 1).fields().get(1).value());
        assertTrue(c.note().startsWith("malloc(sizeof(struct node))"), c.note());
        assertEquals("0x1a60", c.stack().get(1).value(), "the 'new' variable holds the new block's address");
    }

    @Test
    void changedNextPointerIsMarked() {
        ListSnapshot before = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, -1, false),
                new ListSnapshot.NodeView(2, 20, -1, false));
        ListSnapshot after = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, 2, false),
                new ListSnapshot.NodeView(2, 20, -1, false));
        MemoryLayout l = MemoryLayout.ofList(after, before, Map.of(1, 10, 2, 20), MemoryModel.C);
        assertTrue(block(l, 1).fields().get(1).changed());
        assertEquals("0x1a60", block(l, 1).fields().get(1).value());
    }

    @Test
    void cFreesARemovedNodeOnlyInTheStepItDisappears() {
        ListSnapshot two = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, 2, false),
                new ListSnapshot.NodeView(2, 20, -1, false));
        ListSnapshot one = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, -1, false));
        Map<Integer, Integer> seen = Map.of(1, 10, 2, 20);
        MemoryLayout freedNow = MemoryLayout.ofList(one, two, seen, MemoryModel.C);
        assertEquals(BlockState.FREED, block(freedNow, 2).state());
        assertTrue(freedNow.note().startsWith("free(0x1a60)"), freedNow.note());
        MemoryLayout later = MemoryLayout.ofList(one, one, seen, MemoryModel.C);
        assertEquals(1, later.heap().size(), "after free the block is no longer shown");
    }

    @Test
    void javaKeepsUnreachableNodesAsGarbage() {
        ListSnapshot two = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, 2, false),
                new ListSnapshot.NodeView(2, 20, -1, false));
        ListSnapshot one = list(1, Map.of(), new ListSnapshot.NodeView(1, 10, -1, false));
        Map<Integer, Integer> seen = Map.of(1, 10, 2, 20);
        MemoryLayout now = MemoryLayout.ofList(one, two, seen, MemoryModel.JAVA);
        assertEquals(BlockState.GARBAGE, block(now, 2).state());
        assertTrue(now.note().contains("garbage"), now.note());
        MemoryLayout later = MemoryLayout.ofList(one, one, seen, MemoryModel.JAVA);
        assertEquals(BlockState.GARBAGE, block(later, 2).state(), "garbage stays visible until the operation ends");
    }

    @Test
    void treeInsertAllocatesOneBlockAndDeleteReleasesOne() {
        BinarySearchTree t = new BinarySearchTree();
        t.insert(50);
        t.insert(30);
        Map<String, Integer> vars = new LinkedHashMap<>();
        vars.put("root", 1);

        ExecutionTrace<TreeStep> ins = t.insert(70);
        assertEquals(1, countOver(ins, vars, BlockState.NEW), "exactly one malloc for one insert");
        assertEquals(3, MemoryLayout.ofTree(ins.last().tree(), null, vars, null, Map.of(), MemoryModel.C).heap().size());

        ExecutionTrace<TreeStep> del = t.delete(30);
        assertEquals(1, countOver(del, vars, BlockState.FREED), "exactly one free for one delete");
    }

    /** Number of blocks in {@code state} summed over every step of the trace. */
    private static long countOver(ExecutionTrace<TreeStep> trace, Map<String, Integer> vars, BlockState state) {
        long n = 0;
        for (int i = 1; i < trace.size(); i++) {
            n += MemoryLayout.ofTree(trace.get(i).tree(), trace.get(i - 1).tree(), vars, vars, Map.of(), MemoryModel.C)
                    .heap().stream().filter(b -> b.state() == state).count();
        }
        return n;
    }
}
