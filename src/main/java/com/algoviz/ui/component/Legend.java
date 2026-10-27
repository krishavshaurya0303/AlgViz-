package com.algoviz.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/** Colour legend overlay ("Comparing", "Swapping", ...). */
public final class Legend extends HBox {

    public Legend() {
        getStyleClass().add("legend");
        setAlignment(Pos.CENTER_LEFT);
        setMaxWidth(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);
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
