package com.algoviz.engine.sorting;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;

import java.util.List;

/** Top-down Merge Sort with an explicit temporary buffer for each merge. */
public final class MergeSort extends SortingAlgorithm {

    private static final int L_SPLIT = 2;
    private static final int L_MERGE = 5;
    private static final int L_COPY = 6;
    private static final int L_COMPARE = 8;
    private static final int L_TAKE_LEFT = 9;
    private static final int L_TAKE_RIGHT = 10;
    private static final int L_REMAINING = 11;

    private static final AlgorithmMetadata METADATA = new AlgorithmMetadata(
            "merge-sort", "Merge Sort", AlgorithmCategory.SORTING,
            "Divide and conquer: split the array in half, sort each half recursively, then merge the sorted halves.",
            "Guaranteed O(n log n) time and stability; standard for linked lists and external sorting.",
            List.of(
                    "Split the current segment into a left half and a right half.",
                    "Recursively sort both halves (a single element is already sorted).",
                    "Copy the segment into a temporary buffer.",
                    "Repeatedly take the smaller head of the two halves and write it back into the array.",
                    "Taking the left element on ties keeps equal values in their original order (stable)."),
            List.of(
                    "mergeSort(a, lo, hi):",
                    "    if lo >= hi: return",
                    "    mid = (lo + hi) / 2",
                    "    mergeSort(a, lo, mid)",
                    "    mergeSort(a, mid + 1, hi)",
                    "    merge(a, lo, mid, hi)",
                    "merge: copy a[lo..hi] into temp",
                    "    i = lo, j = mid + 1, k = lo",
                    "    while i <= mid and j <= hi:",
                    "        if temp[i] <= temp[j]: a[k++] = temp[i++]",
                    "        else: a[k++] = temp[j++]",
                    "    copy any remaining temp elements into a"),
            "O(n log n)", "O(n log n)", "O(n log n)", "O(n)", true, false, null,
            "Needs an O(n) auxiliary buffer (shown below the bars) plus O(log n) recursion stack.");

    @Override
    public AlgorithmMetadata metadata() {
        return METADATA;
    }

    @Override
    protected void sort(SortRecorder r) {
        mergeSort(r, 0, r.size() - 1);
        r.clearRange();
    }

    private void mergeSort(SortRecorder r, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) >>> 1;
        r.setRange(lo, hi);
        r.note(L_SPLIT, "Split indices " + lo + ".." + hi + " into " + lo + ".." + mid
                + " and " + (mid + 1) + ".." + hi + ".");
        mergeSort(r, lo, mid);
        mergeSort(r, mid + 1, hi);
        merge(r, lo, mid, hi);
    }

    private void merge(SortRecorder r, int lo, int mid, int hi) {
        r.setRange(lo, hi);
        r.loadBuffer(lo, mid, hi);
        r.note(L_COPY, "Merge the sorted halves " + lo + ".." + mid + " and " + (mid + 1) + ".." + hi
                + ". First copy them into the temporary buffer.");
        int i = lo;
        int j = mid + 1;
        int k = lo;
        while (i <= mid && j <= hi) {
            boolean rightFirst = r.compareBufferHeads(L_COMPARE, (o, x, y) -> o
                    ? "Compare left head " + x + " with right head " + y + ". " + y + " comes first."
                    : "Compare left head " + x + " with right head " + y + ". " + x
                    + " comes first (on a tie the left one is taken — this keeps the sort stable).");
            if (!rightFirst) {
                int v = r.bufferAt(i);
                i++;
                r.setBufferHeads(i, j);
                r.write(k, v, L_TAKE_LEFT, "Write " + v + " from the left half into index " + k + ".");
            } else {
                int v = r.bufferAt(j);
                j++;
                r.setBufferHeads(i, j);
                r.write(k, v, L_TAKE_RIGHT, "Write " + v + " from the right half into index " + k + ".");
            }
            k++;
        }
        while (i <= mid) {
            int v = r.bufferAt(i);
            i++;
            r.setBufferHeads(i, j);
            r.write(k, v, L_REMAINING, "The right half is used up. Copy the remaining left element "
                    + v + " into index " + k + ".");
            k++;
        }
        while (j <= hi) {
            int v = r.bufferAt(j);
            j++;
            r.setBufferHeads(i, j);
            r.write(k, v, L_REMAINING, "The left half is used up. Copy the remaining right element "
                    + v + " into index " + k + ".");
            k++;
        }
        r.clearBuffer();
        r.note(lo, hi, L_MERGE, "Indices " + lo + ".." + hi + " are now merged and sorted.");
    }
}
