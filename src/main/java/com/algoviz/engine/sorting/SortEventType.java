package com.algoviz.engine.sorting;

/** The kind of operation a sorting step represents. */
public enum SortEventType {
    START,
    COMPARE,
    SWAP,
    WRITE,
    MARK_SORTED,
    NOTE,
    DONE
}
