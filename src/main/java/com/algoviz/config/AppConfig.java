package com.algoviz.config;

/**
 * Central place for application-wide constants and input limits.
 * Keeping these here avoids "magic numbers" scattered through controllers.
 */
public final class AppConfig {

    public static final String APP_NAME = "AlgoViz";
    public static final String APP_FULL_NAME = "Algorithm Visualizer";
    public static final String VERSION = "1.0.0";

    // Sorting Studio limits
    public static final int SORT_MIN_SIZE = 2;
    public static final int SORT_MAX_SIZE = 20;
    public static final int SORT_DEFAULT_SIZE = 12;
    public static final int SORT_MIN_VALUE = -999;
    public static final int SORT_MAX_VALUE = 999;
    public static final int SORT_RANDOM_MIN = 5;
    public static final int SORT_RANDOM_MAX = 99;

    // Graph Explorer grid dimensions (4-directional movement)
    public static final int GRID_ROWS = 19;
    public static final int GRID_COLS = 35;
    public static final int WEIGHTED_CELL_COST = 5;

    // Tree Visualizer
    public static final int TREE_MAX_NODES = 63;
    public static final int TREE_MIN_VALUE = -999;
    public static final int TREE_MAX_VALUE = 999;

    // Linked List Lab
    public static final int LIST_MAX_NODES = 10;
    public static final int LIST_MIN_VALUE = -999;
    public static final int LIST_MAX_VALUE = 999;

    // Playback speed in steps per second
    public static final double SPEED_MIN = 0.5;
    public static final double SPEED_MAX = 60;
    public static final double SPEED_DEFAULT = 4;

    private AppConfig() {
    }
}
