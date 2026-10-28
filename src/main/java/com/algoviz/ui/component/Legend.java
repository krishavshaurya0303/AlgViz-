package com.algoviz.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Colour legend overlay ("Comparing", "Swapping", ...). One row when the board is
 * wide enough; on a narrow board the entries wrap instead of being cut off.
 */
public final class Legend extends FlowPane {

    /** Gap kept between the legend and the right edge of the board. */
    private static final double EDGE = 24;

    public Legend() {
        getStyleClass().add("legend");
        setAlignment(Pos.CENTER_LEFT);
        setPrefWrapLength(4000); // preferred size is a single row
    }

    @Override
    protected double computeMaxWidth(double height) {
        double single = prefWidth(-1);
        return getParent() instanceof Region r ? Math.min(single, Math.max(0, r.getWidth() - EDGE)) : single;
    }

    @Override
    protected double computeMaxHeight(double width) {
        return prefHeight(width);
    }

    public Legend item(Color color, String label) {
        return item(color, null, label);
    }

    /** Adds an entry; a non-null outline draws a hollow swatch. */
    public Legend item(Color fill, Color outline, String label) {
        Rectangle swatch = new Rectangle(10, 10);
        swatch.setArcWidth(4);
        swatch.setArcHeight(4);
        swatch.setFill(fill);
        if (outline != null) {
            swatch.setStroke(outline);
            swatch.setStrokeWidth(1.5);
        }
        Label l = new Label(label);
        HBox item = new HBox(swatch, l);
        item.getStyleClass().add("legend-item");
        getChildren().add(item);
        return this;
    }

    public void clear() {
        getChildren().clear();
    }
}
