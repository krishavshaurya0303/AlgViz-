package com.algoviz.engine.sorting;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;

import java.util.List;

/**
 * Insertion Sort using shifts (not swaps): the key is lifted out, larger
 * elements are shifted one place right, then the key is written into the gap.
 */
public final class InsertionSort extends SortingAlgorithm {

    private static final int L_KEY = 1;
    private static final int L_COMPARE = 3;
    private static final int L_SHIFT = 4;
    private static final int L_INSERT = 6;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "insertion-sort", "Insertion Sort", AlgorithmCategory.SORTING,
            "Builds a sorted prefix by inserting each new element into its correct place among the earlier ones.",
            "Small or nearly-sorted arrays; also used inside hybrid sorts such as Timsort.",
            List.of(
                    "Treat the first element as a sorted prefix of length one.",
                    "Lift the next element out as the key, leaving a gap.",
                    "Shift every larger element of the prefix one position right.",
                    "Write the key into the gap. The prefix is now one element longer."),
            List.of(
                    "for i = 1 to n - 1:",
                    "    key = a[i]",
                    "    j = i - 1",
                    "    while j >= 0 and a[j] > key:",
                    "        a[j + 1] = a[j]      // shift right",
                    "        j = j - 1",
                    "    a[j + 1] = key"),
            "O(n)", "O(n²)", "O(n²)", "O(1)", true, true, null,
            "Moves are shifts (one write each), so this view counts writes, not swaps.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected void sort(SortRecorder r) {
        int n = r.size();
        for (int i = 1; i < n; i++) {
            int key = r.get(i);
            r.setRange(0, i);
            r.hold(key);
            r.setMarker("gap", i, MarkerRole.GAP);
            r.note(i, -1, L_KEY, "Lift out key = " + key + " from index " + i
                    + ". Elements 0.." + (i - 1) + " are sorted relative to each other.");
            int j = i - 1;
            while (j >= 0) {
                boolean out = r.compareWithHeld(j, L_COMPARE, (o, x, y) -> o
                        ? "Comparing " + x + " with key " + y + ". " + x + " " + r.gt() + " " + y
                        + ", so " + x + " must shift right."
                        : "Comparing " + x + " with key " + y + ". " + x + " is not " + r.bigger()
                        + ", so the key belongs right after it.");
                if (!out) {
                    break;
                }
                int moving = r.get(j);
                r.write(j + 1, moving, L_SHIFT, "Shift " + moving + " from index " + j + " to index " + (j + 1)
                        + ". The gap moves left to index " + j + ".");
                r.setMarker("gap", j, MarkerRole.GAP);
                j--;
            }
            r.release();
            r.clearMarker("gap");
            r.write(j + 1, key, L_INSERT, "Insert key " + key + " into the gap at index " + (j + 1) + ".");
        }
        r.clearRange();
    }
}
