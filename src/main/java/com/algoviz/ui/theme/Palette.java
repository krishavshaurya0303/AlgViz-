package com.algoviz.ui.theme;

import javafx.scene.paint.Color;

/**
 * Colour tokens for custom canvas drawing. Kept in sync with the looked-up
 * colours defined at the top of app.css so canvases and controls match.
 * <p>
 * Theme: "ink on dark paper" — warm charcoal surfaces, paper-white ink and one
 * brick-red accent. The token names describe roles from the original design
 * (INDIGO is the accent, CYAN marks the current element, and so on); the values
 * are muted ink colours. No gradients or glows are drawn anywhere.
 */
public final class Palette {

    public static final Color BG_0 = Color.web("#eadfc8");
    public static final Color BG_1 = Color.web("#f3ebda");
    public static final Color BG_2 = Color.web("#f3ebda");
    public static final Color BG_3 = Color.web("#e4d8bf");
    public static final Color BG_4 = Color.web("#d9cbad");
    public static final Color BORDER = Color.web("#c4b598");
    public static final Color BORDER_STRONG = Color.web("#3f5a62");

    public static final Color TEXT = Color.web("#2b3f45");
    public static final Color TEXT_2 = Color.web("#4a5b60");
    public static final Color TEXT_3 = Color.web("#58666b");

    /** The single UI accent: brick red. */
    public static final Color INDIGO = Color.web("#fe8254");
    public static final Color VIOLET = Color.web("#9c5b86");
    public static final Color BLUE = Color.web("#4a6f9a");
    /** "Current element": blue ink. */
    public static final Color CYAN = Color.web("#3f7f93");
    public static final Color GREEN = Color.web("#4e8a5e");
    public static final Color AMBER = Color.web("#c98a12");
    public static final Color CORAL = Color.web("#b5452e");

    /** Neutral bar / node colour. */
    public static final Color NEUTRAL = Color.web("#8a989c");
    public static final Color NEUTRAL_LIGHT = Color.web("#5f7176");

    // Graph explorer cell colours
    public static final Color CELL_EMPTY = Color.web("#f3ebda");
    public static final Color CELL_LINE = Color.web("#e4d8bf");
    public static final Color CELL_WALL = Color.web("#3f5a62");
    public static final Color CELL_WALL_TOP = Color.web("#557780");
    public static final Color CELL_WEIGHT = Color.web("#ead2a2");
    public static final Color CELL_FRONTIER = Color.web("#a9cad4");
    public static final Color CELL_VISITED = Color.web("#d5ddd6");
    public static final Color CELL_PATH = GREEN;

    private Palette() {
    }

    public static Color withAlpha(Color c, double alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    /** A node fill tinted towards {@code c}: the paper surface with a wash of ink. */
    public static Color tint(Color c) {
        return BG_3.interpolate(c, 0.22);
    }
}
