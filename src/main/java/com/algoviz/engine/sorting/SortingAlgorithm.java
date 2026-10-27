package com.algoviz.engine.sorting;

import com.algoviz.engine.Algorithm;
import com.algoviz.engine.ExecutionTrace;

/**
 * Template for all sorting algorithms (Template Method + Strategy).
 * <p>
 * {@link #run(SortInput)} handles the parts every sort shares — creating the
 * instrumented array, recording the initial and final states, timing — while
 * each subclass implements only its own logic in {@link #sort(SortRecorder)}.
 * Inheritance is genuine here: every concrete class "is a" sorting algorithm.
 */
public abstract class SortingAlgorithm implements Algorithm<SortInput, SortStep> {

    @Override
    public final ExecutionTrace<SortStep> run(SortInput input) {
        long t0 = System.nanoTime();
        SortRecorder recorder = new SortRecorder(input.values(), input.order());
        recorder.start("Initial array with " + input.size() + " element" + (input.size() == 1 ? "" : "s")
                + ". " + name() + " will arrange it in " + input.order().toString().toLowerCase() + " order.");
        if (input.size() > 1) {
            sort(recorder);
        }
        recorder.finish();
        return new ExecutionTrace<>(name(), recorder.steps(), System.nanoTime() - t0);
    }

    /** Convenience for callers that only need the result. */
    public final int[] sortValues(int[] values, SortOrder order) {
        return run(new SortInput(values, order)).last().values();
    }

    /** Sorts the instrumented array. Only called for arrays of 2 or more elements. */
    protected abstract void sort(SortRecorder r);
}
