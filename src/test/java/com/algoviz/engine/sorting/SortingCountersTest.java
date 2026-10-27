package com.algoviz.engine.sorting;

import com.algoviz.engine.ExecutionTrace;
import com.algoviz.model.MetricType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the documented counter definitions on hand-computable inputs. */
class SortingCountersTest {

    private static long metric(ExecutionTrace<SortStep> t, MetricType m) {
        return t.last().metrics().get(m);
    }

    private static ExecutionTrace<SortStep> run(SortingAlgorithm a, int... values) {
        return a.run(new SortInput(values, SortOrder.ASCENDING));
    }

    @Test
    void bubbleSortStopsEarlyOnSortedInput() {
        ExecutionTrace<SortStep> t = run(new BubbleSort(), 1, 2, 3, 4, 5);
        assertEquals(4, metric(t, MetricType.COMPARISONS));
        assertEquals(0, metric(t, MetricType.SWAPS));
    }

    @Test
    void bubbleSortReverseInputSwapsEveryPair() {
        ExecutionTrace<SortStep> t = run(new BubbleSort(), 5, 4, 3, 2, 1);
        assertEquals(10, metric(t, MetricType.COMPARISONS));
        assertEquals(10, metric(t, MetricType.SWAPS));
        assertEquals(20, metric(t, MetricType.WRITES));
    }

    @Test
    void selectionSortAlwaysScansEverything() {
        ExecutionTrace<SortStep> t = run(new SelectionSort(), 1, 2, 3, 4, 5);
        assertEquals(10, metric(t, MetricType.COMPARISONS));
        assertEquals(0, metric(t, MetricType.SWAPS));
        ExecutionTrace<SortStep> r = run(new SelectionSort(), 5, 4, 3, 2, 1);
        assertEquals(10, metric(r, MetricType.COMPARISONS));
        assertEquals(2, metric(r, MetricType.SWAPS));
    }

    @Test
    void insertionSortShiftsAreWritesNotSwaps() {
        ExecutionTrace<SortStep> t = run(new InsertionSort(), 3, 2, 1);
        assertEquals(0, metric(t, MetricType.SWAPS));
        // i=1: compare 3>2, shift 3, place 2 -> 1 cmp, 2 writes
        // i=2: compare 3>1, shift, compare 2>1, shift, place -> 2 cmp, 3 writes
        assertEquals(3, metric(t, MetricType.COMPARISONS));
        assertEquals(5, metric(t, MetricType.WRITES));
        ExecutionTrace<SortStep> sorted = run(new InsertionSort(), 1, 2, 3, 4);
        assertEquals(3, metric(sorted, MetricType.COMPARISONS));
    }

    @Test
    void mergeSortWritesEveryElementPerLevel() {
        ExecutionTrace<SortStep> t = run(new MergeSort(), 4, 3, 2, 1);
        assertEquals(8, metric(t, MetricType.WRITES)); // 2 levels * 4 elements
        assertEquals(0, metric(t, MetricType.SWAPS));
        assertEquals(4, metric(t, MetricType.COMPARISONS));
        boolean bufferShown = t.steps().stream().anyMatch(SortStep::hasBuffer);
        assertTrue(bufferShown);
        assertFalse(t.last().hasBuffer());
    }

    @Test
    void quickSortWorstCaseOnSortedInput() {
        ExecutionTrace<SortStep> t = run(new QuickSort(), 1, 2, 3, 4, 5);
        assertEquals(10, metric(t, MetricType.COMPARISONS)); // n(n-1)/2 with last-element pivot
        assertEquals(0, metric(t, MetricType.SWAPS));
        boolean pivotShown = t.steps().stream().anyMatch(s -> s.markers().stream()
                .anyMatch(m -> m.role() == MarkerRole.PIVOT));
        assertTrue(pivotShown);
    }

    @Test
    void descendingReversesComparisons() {
        int[] out = new QuickSort().sortValues(new int[]{1, 3, 2}, SortOrder.DESCENDING);
        assertEquals(3, out[0]);
        assertEquals(1, out[2]);
    }

    @Test
    void insertionSortHoldsKeyDuringShifts() {
        ExecutionTrace<SortStep> t = run(new InsertionSort(), 2, 1);
        assertTrue(t.steps().stream().anyMatch(s -> s.isHolding() && s.heldValue() == 1));
        assertFalse(t.last().isHolding());
    }
}
