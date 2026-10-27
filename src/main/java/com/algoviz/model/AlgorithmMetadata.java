package com.algoviz.model;

import java.util.List;
import java.util.Objects;

/**
 * Descriptive, documented facts about an algorithm or data structure.
 * These are theoretical characteristics — never measured from a run.
 *
 * @param stable  TRUE/FALSE for sorting algorithms, null where stability is not meaningful
 * @param inPlace TRUE/FALSE where meaningful, otherwise null
 */
public record AlgorithmMetadata(
        String id,
        String name,
        AlgorithmCategory category,
        String summary,
        String whenUseful,
        List<String> howItWorks,
        List<String> pseudocode,
        String bestTime,
        String averageTime,
        String worstTime,
        String space,
        Boolean stable,
        Boolean inPlace,
        List<ComplexityEntry> operations,
        String complexityNote) {

    public AlgorithmMetadata {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        Objects.requireNonNull(category);
        howItWorks = List.copyOf(howItWorks);
        pseudocode = List.copyOf(pseudocode);
        operations = operations == null ? List.of() : List.copyOf(operations);
    }
}
