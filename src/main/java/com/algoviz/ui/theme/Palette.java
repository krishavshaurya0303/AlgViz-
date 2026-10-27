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

    public static final Color BG_0 = Color.web("#1b1916");
    public static final Color BG_1 = Color.web("#201d1a");
    public static final Color BG_2 = Color.web("#25221e");
    public static final Color BG_3 = Color.web("#2c2824");
    public static final Color BG_4 = Color.web("#36312b");
    public static final Color BORDER = Color.web("#38332c");
    public static final Color BORDER_STRONG = Color.web("#4d463d");

    public static final Color TEXT = Color.web("#ece4d4");
    public static final Color TEXT_2 = Color.web("#b8ad9b");
    public static final Color TEXT_3 = Color.web("#857b6c");

    /** The single UI accent: brick red. */
    public static final Color INDIGO = Color.web("#c8553d");
    public static final Color VIOLET = Color.web("#a88aa6");
    public static final Color BLUE = Color.web("#7f9cc0");
    /** "Current element": blue ink. */
    public static final Color CYAN = Color.web("#86b4c9");
    public static final Color GREEN = Color.web("#8fb573");
    public static final Color AMBER = Color.web("#d9a441");
    public static final Color CORAL = Color.web("#e07a62");

    /** Neutral bar / node colour. */
    public static final Color NEUTRAL = Color.web("#6b6255");
    public static final Color NEUTRAL_LIGHT = Color.web("#8a7f6f");

    // Graph explorer cell colours
    public static final Color CELL_EMPTY = Color.web("#221f1b");
    public static final Color CELL_LINE = Color.web("#2d2924");
    public static final Color CELL_WALL = Color.web("#4a4339");
    public static final Color CELL_WALL_TOP = Color.web("#5c5447");
    public static final Color CELL_WEIGHT = Color.web("#3d3122");
    public static final Color CELL_FRONTIER = Color.web("#4b7182");
    public static final Color CELL_VISITED = Color.web("#46506a");
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
