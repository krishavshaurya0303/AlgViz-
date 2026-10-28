package com.algoviz.engine.sorting;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;

import java.util.List;

/** Bubble Sort with the early-exit optimisation (stops after a pass with no swaps). */
public final class BubbleSort extends SortingAlgorithm {

    private static final int L_PASS = 0;
    private static final int L_COMPARE = 3;
    private static final int L_SWAP = 4;
    private static final int L_MARK = 6;
    private static final int L_EARLY_EXIT = 7;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "bubble-sort", "Bubble Sort", AlgorithmCategory.SORTING,
            "Repeatedly compares adjacent elements and swaps them when they are in the wrong order.",
            "Teaching, or tiny / nearly-sorted arrays where its early exit makes it O(n).",
            List.of(
                    "Walk through the array comparing each pair of neighbours.",
                    "Swap a pair when the left element is larger.",
                    "After each pass the largest remaining element has 'bubbled' to the end and is final.",
                    "If a whole pass makes no swaps, the array is already sorted and the algorithm stops."),
            List.of(
                    "for pass = 0 to n - 2:",
                    "    swapped = false",
                    "    for j = 0 to n - pass - 2:",
                    "        if a[j] > a[j + 1]:",
                    "            swap(a[j], a[j + 1])",
                    "            swapped = true",
                    "    a[n - pass - 1] is now final",
                    "    if not swapped: stop early"),
            "O(n)", "O(n²)", "O(n²)", "O(1)", true, true, null,
            "Best case O(n) applies to an already-sorted array thanks to the early exit.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected void sort(SortRecorder r) {
        int n = r.size();
        for (int pass = 0; pass < n - 1; pass++) {
            int end = n - pass - 1;
            r.setRange(0, end);
            r.note(L_PASS, "Pass " + (pass + 1) + ": walk through indices 0.." + end
                    + " and push the " + r.bigger() + " value towards index " + end + ".");
            boolean swapped = false;
            for (int j = 0; j < end; j++) {
                boolean out = r.compare(j, j + 1, L_COMPARE, (o, x, y) -> o
                        ? "Comparing adjacent elements " + x + " and " + y + ". " + x + " " + r.gt() + " " + y
                        + ", so they are in the wrong order."
                        : "Comparing adjacent elements " + x + " and " + y + ". They are already in order, so nothing moves.");
                if (out) {
                    int x = r.get(j);
                    int y = r.get(j + 1);
                    r.swap(j, j + 1, L_SWAP, "Swap " + x + " and " + y + " so the " + r.bigger()
                            + " value moves one place right.");
                    swapped = true;
                }
            }
            r.markSorted(end, L_MARK, r.get(end) + " has bubbled to index " + end + " — its final position.");
            if (!swapped) {
                r.note(L_EARLY_EXIT, "No swaps happened in this pass, so the remaining elements are already "
                        + "in order. Bubble Sort stops early.");
                break;
            }
        }
        r.clearRange();
    }
}
