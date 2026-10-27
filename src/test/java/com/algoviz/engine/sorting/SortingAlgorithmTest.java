package com.algoviz.engine.sorting;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.MetricType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every sorting implementation is verified independently against Arrays.sort. */
class SortingAlgorithmTest {

    static Stream<SortingAlgorithm> algorithms() {
        return Stream.of(new BubbleSort(), new SelectionSort(), new InsertionSort(), new MergeSort(), new QuickSort());
    }

    static Stream<Arguments> cases() {
        List<int[]> inputs = new ArrayList<>(List.of(
                new int[]{},
                new int[]{42},
                new int[]{1, 2},
                new int[]{2, 1},
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                new int[]{8, 7, 6, 5, 4, 3, 2, 1},
                new int[]{5, 1, 5, 3, 1, 5, 3},
                new int[]{7, 7, 7, 7, 7},
                new int[]{-3, 10, -50, 0, 999, -999, 4},
                new int[]{0, -1, -1, 0, 1}));
        Random rnd = new Random(20261027L);
        for (int k = 0; k < 40; k++) {
            int n = 2 + rnd.nextInt(149);
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = rnd.nextInt(1999) - 999;
            }
            inputs.add(a);
        }
        List<Arguments> out = new ArrayList<>();
        algorithms().forEach(alg -> {
            for (int[] in : inputs) {
                for (SortOrder order : SortOrder.values()) {
                    out.add(Arguments.of(alg, in, order));
                }
            }
        });
        return out.stream();
    }

    private static int[] expected(int[] in, SortOrder order) {
        int[] e = in.clone();
        Arrays.sort(e);
        if (order == SortOrder.DESCENDING) {
            for (int i = 0, j = e.length - 1; i < j; i++, j--) {
                int t = e[i];
                e[i] = e[j];
                e[j] = t;
            }
        }
        return e;
    }

    @ParameterizedTest(name = "{0} {2} n={1}")
    @MethodSource("cases")
    void sortsCorrectly(SortingAlgorithm alg, int[] input, SortOrder order) {
        ExecutionTrace<SortStep> trace = alg.run(new SortInput(input, order));
        int[] want = expected(input, order);

        assertArrayEquals(input, trace.first().values(), "first step must show the untouched input");
        assertArrayEquals(want, trace.last().values(), "final array must be sorted");
        assertEquals(SortEventType.DONE, trace.last().type());
        for (int i = 0; i < want.length; i++) {
            assertTrue(trace.last().isSorted(i));
        }
    }

    @ParameterizedTest(name = "{0} {2} n={1}")
    @MethodSource("cases")
    void stepsAreConsistent(SortingAlgorithm alg, int[] input, SortOrder order) {
        ExecutionTrace<SortStep> trace = alg.run(new SortInput(input, order));
        int[] want = expected(input, order);
        int[] multiset = input.clone();
        Arrays.sort(multiset);
        long prevCmp = 0;
        long prevSwaps = 0;
        long prevWrites = 0;
        for (SortStep s : trace.steps()) {
            // Counters never decrease.
            long cmp = s.metrics().get(MetricType.COMPARISONS);
            long swaps = s.metrics().get(MetricType.SWAPS);
            long writes = s.metrics().get(MetricType.WRITES);
            assertTrue(cmp >= prevCmp && swaps >= prevSwaps && writes >= prevWrites);
            // Each comparison step adds exactly one comparison, each swap exactly one swap.
            if (s.type() == SortEventType.COMPARE) {
                assertEquals(prevCmp + 1, cmp);
            }
            if (s.type() == SortEventType.SWAP) {
                assertEquals(prevSwaps + 1, swaps);
                assertEquals(prevWrites + 2, writes);
            }
            if (s.type() == SortEventType.WRITE) {
                assertEquals(prevWrites + 1, writes);
            }
            prevCmp = cmp;
            prevSwaps = swaps;
            prevWrites = writes;

            // An element is only shown as "sorted" once it really holds its final value.
            for (int i = 0; i < s.size(); i++) {
                if (s.isSorted(i)) {
                    assertEquals(want[i], s.valueAt(i), alg.name() + ": index " + i + " marked sorted too early");
                }
            }
            // Swap-based algorithms always hold a permutation of the input.
            if (!(alg instanceof InsertionSort) && !(alg instanceof MergeSort)) {
                int[] now = s.values();
                Arrays.sort(now);
                assertArrayEquals(multiset, now);
            }
            assertTrue(s.explanation() != null && !s.explanation().isBlank());
            assertTrue(s.pseudocodeLine() < alg.metadata().pseudocode().size());
        }
    }

    @ParameterizedTest
    @MethodSource("algorithms")
    void doesNotModifyCallerArray(SortingAlgorithm alg) {
        int[] in = {3, 1, 2};
        alg.run(new SortInput(in, SortOrder.ASCENDING));
        assertArrayEquals(new int[]{3, 1, 2}, in);
    }
}
