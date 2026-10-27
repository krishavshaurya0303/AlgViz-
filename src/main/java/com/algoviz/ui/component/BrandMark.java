package com.algoviz.ui.component;

import javafx.scene.layout.Pane;
import com.algoviz.ui.theme.Palette;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * The AlgoViz logo: a flat brick-red tile holding three ascending bars (sorting)
 * whose tops are connected like graph nodes.
 */
public final class BrandMark extends Pane {

    public BrandMark(double size) {
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        double s = size / 32.0;

        Rectangle tile = new Rectangle(0, 0, size, size);
        tile.setArcWidth(4 * s);
        tile.setArcHeight(4 * s);
        tile.setFill(Palette.INDIGO);
        getChildren().add(tile);

        double[] xs = {9, 16, 23};
        double[] tops = {19, 14, 9};
        for (int i = 0; i < 3; i++) {
            Line bar = new Line(xs[i] * s, 25 * s, xs[i] * s, tops[i] * s);
            bar.setStroke(Palette.withAlpha(Palette.TEXT, 0.55));
            bar.setStrokeWidth(3.2 * s);
            bar.setStrokeLineCap(StrokeLineCap.ROUND);
            getChildren().add(bar);
        }
        for (int i = 0; i < 2; i++) {
            Line link = new Line(xs[i] * s, tops[i] * s, xs[i + 1] * s, tops[i + 1] * s);
            link.setStroke(Palette.TEXT);
            link.setStrokeWidth(1.6 * s);
            getChildren().add(link);
        }
        for (int i = 0; i < 3; i++) {
            Circle node = new Circle(xs[i] * s, tops[i] * s, 2.6 * s, Palette.TEXT);
            getChildren().add(node);
        }
    }
}
