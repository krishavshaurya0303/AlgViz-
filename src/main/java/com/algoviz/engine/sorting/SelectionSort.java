package com.algoviz.engine.sorting;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;

import java.util.List;

/** Selection Sort: repeatedly selects the minimum of the unsorted section. */
public final class SelectionSort extends SortingAlgorithm {

    private static final int L_ASSUME = 1;
    private static final int L_COMPARE = 3;
    private static final int L_NEW_MIN = 4;
    private static final int L_SWAP = 5;
    private static final int L_MARK = 6;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "selection-sort", "Selection Sort", AlgorithmCategory.SORTING,
            "Finds the smallest element of the unsorted section and moves it to the front of that section.",
            "When writes are expensive: it performs at most n − 1 swaps.",
            List.of(
                    "Split the array into a sorted prefix (initially empty) and an unsorted suffix.",
                    "Scan the whole unsorted suffix to find its minimum.",
                    "Swap the minimum into the first unsorted position; the prefix grows by one.",
                    "It always does the full scan, so even sorted input costs O(n²) comparisons."),
            List.of(
                    "for i = 0 to n - 2:",
                    "    min = i",
                    "    for j = i + 1 to n - 1:",
                    "        if a[j] < a[min]:",
                    "            min = j",
                    "    if min != i: swap(a[i], a[min])",
                    "    a[i] is now in its final place"),
            "O(n²)", "O(n²)", "O(n²)", "O(1)", false, true, null,
            "Not stable: the long-distance swap can move an element past an equal one.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected void sort(SortRecorder r) {
        int n = r.size();
        String target = r.order() == SortOrder.ASCENDING ? "minimum" : "maximum";
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            r.setRange(i, n - 1);
            r.setMarker(target, min, MarkerRole.MINIMUM);
            r.note(L_ASSUME, "Start of the unsorted section is index " + i + ". Assume " + r.get(i)
                    + " is the " + target + " and search the rest of the section.");
            for (int j = i + 1; j < n; j++) {
                final int currentMin = min;
                boolean better = r.compare(min, j, L_COMPARE, (o, x, y) -> o
                        ? "Comparing " + y + " with the current " + target + " " + x + ". " + y
                        + " should come first."
                        : "Comparing " + y + " with the current " + target + " " + x + ". The " + target
                        + " is still " + x + " (index " + currentMin + ").");
                if (better) {
                    min = j;
                    r.setMarker(target, min, MarkerRole.MINIMUM);
                    r.note(min, -1, L_NEW_MIN, "The current " + target + " is now " + r.get(min)
                            + " at index " + min + ". Keep searching the remaining unsorted section.");
                }
            }
            if (min != i) {
                int x = r.get(i);
                int y = r.get(min);
                r.swap(i, min, L_SWAP, "Swap the " + target + " " + y + " into index " + i
                        + " (exchanging it with " + x + ").");
            } else {
                r.note(i, -1, L_SWAP, r.get(i) + " is already the " + target + ", so no swap is needed.");
            }
            r.clearMarker(target);
            r.markSorted(i, L_MARK, r.get(i) + " is now in its final position at index " + i + ".");
        }
        r.clearRange();
        r.markSorted(n - 1, L_MARK, "Only one element remains, so " + r.get(n - 1) + " is also in its final place.");
    }
}
