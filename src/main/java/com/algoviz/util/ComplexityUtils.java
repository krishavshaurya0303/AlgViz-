package com.algoviz.util;

import java.util.Locale;

/** Formatting helpers for metrics and durations. */
public final class ComplexityUtils {

    private ComplexityUtils() {
    }

    /** Formats elapsed time as "0.84 s", "12.3 s" or "2:05". */
    public static String formatElapsed(long nanos) {
        double seconds = nanos / 1e9;
        if (seconds < 10) {
            return String.format(Locale.ROOT, "%.2f s", seconds);
        }
        if (seconds < 60) {
            return String.format(Locale.ROOT, "%.1f s", seconds);
        }
        long s = (long) seconds;
        return String.format(Locale.ROOT, "%d:%02d", s / 60, s % 60);
    }

    /** Formats a short duration such as trace-build time. */
    public static String formatBuildTime(long nanos) {
        double ms = nanos / 1e6;
        return ms < 1 ? String.format(Locale.ROOT, "%.0f µs", nanos / 1e3)
                : String.format(Locale.ROOT, "%.1f ms", ms);
    }

    public static String formatCount(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }
}
