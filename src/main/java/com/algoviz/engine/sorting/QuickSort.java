package com.algoviz.engine.sorting;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;

import java.util.List;

/** Quick Sort with Lomuto partitioning (last element of the partition is the pivot). */
public final class QuickSort extends SortingAlgorithm {

    private static final int L_BASE = 1;
    private static final int L_PARTITION = 2;
    private static final int L_PIVOT = 5;
    private static final int L_COMPARE = 8;
    private static final int L_SWAP = 9;
    private static final int L_PLACE_PIVOT = 11;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "quick-sort", "Quick Sort", AlgorithmCategory.SORTING,
            "Divide and conquer: pick a pivot, partition the array into smaller and larger elements, then recurse.",
            "Fast in practice on random data, sorts in place; the basis of many library sorts.",
            List.of(
                    "Choose the last element of the partition as the pivot (Lomuto scheme).",
                    "Scan the partition; move every element that is ≤ pivot into the left region.",
                    "Swap the pivot just after the left region — it is now in its final position.",
                    "Recursively sort the region left of the pivot and the region right of it."),
            List.of(
                    "quickSort(a, lo, hi):",
                    "    if lo >= hi: return",
                    "    p = partition(a, lo, hi)",
                    "    quickSort(a, lo, p - 1)",
                    "    quickSort(a, p + 1, hi)",
                    "partition: pivot = a[hi]",
                    "    i = lo",
                    "    for j = lo to hi - 1:",
                    "        if a[j] <= pivot:",
                    "            if i != j: swap(a[i], a[j])",
                    "            i = i + 1",
                    "    if i != hi: swap(a[i], a[hi])",
                    "    return i"),
            "O(n log n)", "O(n log n)", "O(n²)", "O(log n) average, O(n) worst", false, true, null,
            "With the last element as pivot, already-sorted input triggers the O(n²) worst case.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected void sort(SortRecorder r) {
        quickSort(r, 0, r.size() - 1);
        r.clearRange();
    }

    private void quickSort(SortRecorder r, int lo, int hi) {
        if (lo > hi) {
            return;
        }
        if (lo == hi) {
            r.setRange(lo, hi);
            r.markSorted(lo, L_BASE, "A partition of a single element (" + r.get(lo)
                    + ") is already in its final position.");
            return;
        }
        r.setRange(lo, hi);
        r.note(L_PARTITION, "Partition indices " + lo + ".." + hi + ".");
        int p = partition(r, lo, hi);
        quickSort(r, lo, p - 1);
        quickSort(r, p + 1, hi);
    }

    private int partition(SortRecorder r, int lo, int hi) {
        int pivot = r.get(hi);
        String le = r.order() == SortOrder.ASCENDING ? "≤" : "≥";
        r.setMarker("pivot", hi, MarkerRole.PIVOT);
        r.setMarker("i", lo, MarkerRole.POINTER);
        r.note(hi, -1, L_PIVOT, "Choose the last element " + pivot + " as the pivot. Elements " + le + " "
                + pivot + " will be moved to the left of index i.");
        int i = lo;
        for (int j = lo; j < hi; j++) {
            boolean out = r.compare(j, hi, L_COMPARE, (o, x, y) -> o
                    ? "Compare " + x + " with pivot " + y + ". " + x + " " + r.gt() + " " + y
                    + ", so it stays in the right region."
                    : "Compare " + x + " with pivot " + y + ". " + x + " " + le + " " + y
                    + ", so it belongs in the left region.");
            if (!out) {
                if (i != j) {
                    int x = r.get(i);
                    int y = r.get(j);
                    r.swap(i, j, L_SWAP, "Swap " + y + " into the left region at index " + i
                            + " (exchanging it with " + x + ").");
                } else {
                    r.note(j, -1, L_SWAP, r.get(j) + " is already at the edge of the left region; no swap needed.");
                }
                i++;
                r.setMarker("i", Math.min(i, hi), MarkerRole.POINTER);
            }
        }
        r.clearMarker("i");
        if (i != hi) {
            r.swap(i, hi, L_PLACE_PIVOT, "Move the pivot " + pivot + " to index " + i
                    + ". Everything left of it is " + le + " " + pivot + ", everything right of it is not.");
        } else {
            r.note(hi, -1, L_PLACE_PIVOT, "The pivot " + pivot + " is already at index " + i + ".");
        }
        r.clearMarker("pivot");
        r.markSorted(i, L_PLACE_PIVOT, "The pivot " + pivot + " is now in its final position at index " + i
                + ". It divides the partition into two regions.");
        return i;
    }
}
