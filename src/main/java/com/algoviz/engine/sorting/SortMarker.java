package com.algoviz.engine.sorting;

/** A labelled index shown under the bars, e.g. "pivot" at index 7. */
public record SortMarker(int index, String label, MarkerRole role) {
}
