package com.algoviz.engine;

import com.algoviz.model.AlgorithmMetadata;

/**
 * Strategy interface for every visualizable algorithm.
 * <p>
 * Different algorithms (Bubble Sort, BFS, ...) are interchangeable behind this
 * interface; the controllers simply call {@link #run(Object)} and play back the
 * resulting trace, whichever concrete algorithm the user selected.
 *
 * @param <I> input type (e.g. an array, a grid)
 * @param <S> step type produced
 */
public interface Algorithm<I, S extends ExecutionStep> {

    AlgorithmMetadata metadata();

    /** Executes the algorithm on the input and records every logical step. */
    ExecutionTrace<S> run(I input);

    default String name() {
        return metadata().name();
    }
}
