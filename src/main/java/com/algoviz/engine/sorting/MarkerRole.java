package com.algoviz.engine.sorting;

/** Role of a labelled index marker drawn under a bar. */
public enum MarkerRole {
    /** Quick Sort pivot. */
    PIVOT,
    /** Selection Sort current minimum. */
    MINIMUM,
    /** Insertion Sort gap where the key will be placed. */
    GAP,
    /** A plain index pointer such as i or j. */
    POINTER
}
