package com.algoviz.ui.theme;

import javafx.scene.paint.Color;

/**
 * Colour tokens for custom canvas drawing. Kept in sync with the looked-up
 * colours defined at the top of app.css so canvases and controls match.
 */
public final class Palette {

    public static final Color BG_0 = Color.web("#0a0e18");
    public static final Color BG_1 = Color.web("#0f1422");
    public static final Color BG_2 = Color.web("#141a2b");
    public static final Color BG_3 = Color.web("#1a2135");
    public static final Color BG_4 = Color.web("#222a42");
    public static final Color BORDER = Color.web("#222a40");
    public static final Color BORDER_STRONG = Color.web("#2f3956");

    public static final Color TEXT = Color.web("#e6e9f2");
    public static final Color TEXT_2 = Color.web("#a6aec4");
    public static final Color TEXT_3 = Color.web("#6d7693");

    public static final Color INDIGO = Color.web("#6c72ff");
    public static final Color VIOLET = Color.web("#9b7bff");
    public static final Color BLUE = Color.web("#4c8dff");
    public static final Color CYAN = Color.web("#38d6e8");
    public static final Color GREEN = Color.web("#3ddc97");
    public static final Color AMBER = Color.web("#ffb547");
    public static final Color CORAL = Color.web("#ff6b6b");

    /** Neutral bar / node colour. */
    public static final Color NEUTRAL = Color.web("#4b5680");
    public static final Color NEUTRAL_LIGHT = Color.web("#5e6a99");

    // Graph explorer cell colours
    public static final Color CELL_EMPTY = Color.web("#111728");
    public static final Color CELL_LINE = Color.web("#1b2236");
    public static final Color CELL_WALL = Color.web("#3a4466");
    public static final Color CELL_WALL_TOP = Color.web("#4a5680");
    public static final Color CELL_WEIGHT = Color.web("#3b2f1c");
    public static final Color CELL_FRONTIER = Color.web("#1d5c6e");
    public static final Color CELL_VISITED = Color.web("#2a2f6e");
    public static final Color CELL_PATH = GREEN;

    private Palette() {
    }

    public static Color withAlpha(Color c, double alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}
