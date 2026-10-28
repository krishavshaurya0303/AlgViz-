package com.algoviz.service;

import com.algoviz.engine.graph.BreadthFirstSearch;
import com.algoviz.engine.graph.DepthFirstSearch;
import com.algoviz.engine.graph.Dijkstra;
import com.algoviz.engine.linkedlist.ListOperation;
import com.algoviz.engine.sorting.BubbleSort;
import com.algoviz.engine.sorting.InsertionSort;
import com.algoviz.engine.sorting.MergeSort;
import com.algoviz.engine.sorting.QuickSort;
import com.algoviz.engine.sorting.SelectionSort;
import com.algoviz.engine.tree.TreeOperation;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.CodeListing;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every pseudocode listing has a C listing, and every pseudocode line maps to at least one C line. */
class CSourceCatalogTest {

    static Stream<Arguments> listings() {
        List<Arguments> out = new ArrayList<>();
        for (AlgorithmMetadata m : List.of(new BubbleSort().metadata(), new SelectionSort().metadata(),
                new InsertionSort().metadata(), new MergeSort().metadata(), new QuickSort().metadata())) {
            out.add(Arguments.of("sorting/" + m.id(), m.pseudocode()));
        }
        for (AlgorithmMetadata m : List.of(new BreadthFirstSearch().metadata(), new DepthFirstSearch().metadata(),
                new Dijkstra().metadata())) {
            out.add(Arguments.of("graph/" + m.id(), m.pseudocode()));
        }
        for (TreeOperation op : TreeOperation.values()) {
            if (op != TreeOperation.VIEW) {
                out.add(Arguments.of("tree/" + op.name(), op.pseudocode()));
            }
        }
        for (ListOperation op : ListOperation.values()) {
            out.add(Arguments.of("list/" + op.name(), op.pseudocode()));
        }
        return out.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("listings")
    void everyPseudocodeLineHasCLines(String key, List<String> pseudocode) {
        CodeListing c = CSourceCatalog.find(key).orElseThrow(() -> new AssertionError("missing C listing " + key));
        assertFalse(c.lines().isEmpty());
        for (int p = 0; p < pseudocode.size(); p++) {
            assertFalse(c.linesFor(p).isEmpty(), key + ": pseudocode line " + (p + 1) + " has no C line");
        }
        for (Set<Integer> mark : c.marks()) {
            for (int p : mark) {
                assertTrue(p >= 0 && p < pseudocode.size(), key + ": tag " + p + " is outside the pseudocode");
            }
        }
        for (String line : c.lines()) {
            assertFalse(line.contains("//@"), key + ": tag left in the displayed code");
        }
    }
}
