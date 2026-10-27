package com.algoviz.ui.component;

/**
 * Vector icons drawn on a 24×24 grid. Stroke icons are outlined, fill icons solid.
 * All paths were designed for AlgoViz (simple geometric shapes, no third-party assets).
 */
public enum IconType {
    DASHBOARD("M4 4h6.5v6.5H4z M13.5 4H20v6.5h-6.5z M4 13.5h6.5V20H4z M13.5 13.5H20V20h-6.5z", false),
    SORTING("M5 20v-6 M9.7 20V8 M14.3 20v-9 M19 20V4", false),
    GRAPH("M3.8 7a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0 M15.8 6a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0 M9.8 18a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0 M8.2 6.8l7.6-.6 M7.1 8.9l3.8 7.2 M17 8.1l-4 7.8", false),
    TREE("M9.8 5a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0 M3.8 18a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0 M15.8 18a2.2 2.2 0 1 0 4.4 0a2.2 2.2 0 1 0 -4.4 0 M10.9 6.9L7.1 16 M13.1 6.9l3.8 9.1", false),
    LINKED_LIST("M2.5 9h6v6h-6z M15.5 9h6v6h-6z M8.5 12h5.5 M12 10l2 2-2 2", false),
    LIBRARY("M5 4.5h10.5a3 3 0 0 1 3 3V20H8a3 3 0 0 1-3-3z M5 17a3 3 0 0 1 3-3h10.5 M9 8.5h6", false),
    SETTINGS("M4 7h9 M17 7h3 M15 5v4 M4 17h3 M11 17h9 M9 15v4 M4 12h12 M20 12h0", false),
    PLAY("M8 5.5v13l11-6.5z", true),
    PAUSE("M7 5h3.5v14H7z M13.5 5H17v14h-3.5z", true),
    STEP_FORWARD("M6 5.5v13l9-6.5z M16 5.5h2.5v13H16z", true),
    STEP_BACK("M18 5.5v13l-9-6.5z M8 5.5H5.5v13H8z", true),
    SKIP_END("M4 5.5v13l8-6.5z M12 5.5v13l8-6.5z", true),
    RESET("M4.5 12a7.5 7.5 0 1 0 2.2-5.3 M4.5 4.5v4.5H9", false),
    SHUFFLE("M3.5 7h3.5l10 10h3.5 M3.5 17h3.5l3.2-3.2 M13.8 10.2L17 7h3.5 M18 4.5L20.5 7 18 9.5 M18 14.5l2.5 2.5-2.5 2.5", false),
    HELP("M12 21a9 9 0 1 0 0-18a9 9 0 1 0 0 18z M9.6 9.3a2.5 2.5 0 1 1 3.4 2.3c-.6.3-1 .8-1 1.5v.6 M12 16.9v.1", false),
    INFO("M12 21a9 9 0 1 0 0-18a9 9 0 1 0 0 18z M12 11v5.5 M12 7.6v.1", false),
    CHECK("M5 12.5l4.2 4.2L19 7", false),
    ALERT("M12 3.5l9 16H3z M12 9.5v4.5 M12 16.8v.1", false),
    ARROW_RIGHT("M5 12h14 M13 6l6 6-6 6", false),
    PLUS("M12 5v14 M5 12h14", false),
    MINUS("M5 12h14", false),
    SEARCH("M10.5 17a6.5 6.5 0 1 0 0-13a6.5 6.5 0 1 0 0 13z M15.5 15.5L20 20", false),
    TRASH("M4.5 7h15 M9.5 7V4.5h5V7 M6.5 7l1 12.5h9l1-12.5", false),
    WALL("M3.5 5.5h17v13h-17z M3.5 10h17 M3.5 14.5h17 M9 5.5V10 M15 10v4.5 M9 14.5v4", false),
    WEIGHT("M7 9.5h10l2 10H5z M12 9.5a2.5 2.5 0 1 0 0-5a2.5 2.5 0 1 0 0 5z", false),
    FLAG("M5.5 21V4 M5.5 4.5h11l-2.5 4 2.5 4h-11", false),
    PIN("M12 21s-6.5-6-6.5-11a6.5 6.5 0 0 1 13 0c0 5-6.5 11-6.5 11z M12 12.5a2.5 2.5 0 1 0 0-5a2.5 2.5 0 1 0 0 5z", false),
    ERASER("M8.5 19.5h11 M4.6 14.8l9.2-9.2a2 2 0 0 1 2.8 0l2.8 2.8a2 2 0 0 1 0 2.8l-7.8 7.8H8.5z M9.5 9.8l4.7 4.7", false),
    REVERSE("M4 8h14 M14 4l4 4-4 4 M20 16H6 M10 12l-4 4 4 4", false),
    SPARK("M12 3v4 M12 17v4 M3 12h4 M17 12h4 M6 6l2.5 2.5 M15.5 15.5L18 18 M6 18l2.5-2.5 M15.5 8.5L18 6", false);

    private final String path;
    private final boolean filled;

    IconType(String path, boolean filled) {
        this.path = path;
        this.filled = filled;
    }

    public String path() {
        return path;
    }

    public boolean filled() {
        return filled;
    }
}
