package com.algoviz.service;

import com.algoviz.model.AlgorithmMetadata;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlgorithmCatalogTest {

    private final AlgorithmCatalog catalog = new AlgorithmCatalog();

    @Test
    void containsEveryAlgorithmOnceWithDocumentation() {
        assertEquals(5, catalog.sortingAlgorithms().size());
        assertEquals(3, catalog.graphAlgorithms().size());
        Set<String> ids = new HashSet<>();
        for (AlgorithmMetadata m : catalog.all()) {
            assertTrue(ids.add(m.id()), "duplicate id " + m.id());
            assertFalse(m.pseudocode().isEmpty());
            assertFalse(m.howItWorks().isEmpty());
            assertFalse(m.worstTime().isBlank());
        }
        assertEquals(10, ids.size());
        assertTrue(catalog.sortingById("merge-sort").isPresent());
        assertTrue(catalog.graphById("dijkstra").isPresent());
    }

    @Test
    void stabilityFactsAreCorrect() {
        assertEquals(Boolean.TRUE, catalog.byId("bubble-sort").orElseThrow().stable());
        assertEquals(Boolean.TRUE, catalog.byId("insertion-sort").orElseThrow().stable());
        assertEquals(Boolean.TRUE, catalog.byId("merge-sort").orElseThrow().stable());
        assertEquals(Boolean.FALSE, catalog.byId("selection-sort").orElseThrow().stable());
        assertEquals(Boolean.FALSE, catalog.byId("quick-sort").orElseThrow().stable());
    }
}
