package com.algoviz.ui.component;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * The AlgoViz logo: a rounded tile holding three ascending bars (sorting)
 * whose tops are connected like graph nodes.
 */
public final class BrandMark extends Pane {

    public BrandMark(double size) {
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        double s = size / 32.0;

        Rectangle tile = new Rectangle(0, 0, size, size);
        tile.setArcWidth(9 * s * 2);
        tile.setArcHeight(9 * s * 2);
        tile.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#7a7fff")), new Stop(1, Color.web("#9b5cff"))));
        getChildren().add(tile);

        double[] xs = {9, 16, 23};
        double[] tops = {19, 14, 9};
        for (int i = 0; i < 3; i++) {
            Line bar = new Line(xs[i] * s, 25 * s, xs[i] * s, tops[i] * s);
            bar.setStroke(Color.rgb(255, 255, 255, 0.55));
            bar.setStrokeWidth(3.2 * s);
            bar.setStrokeLineCap(StrokeLineCap.ROUND);
            getChildren().add(bar);
        }
        for (int i = 0; i < 2; i++) {
            Line link = new Line(xs[i] * s, tops[i] * s, xs[i + 1] * s, tops[i + 1] * s);
            link.setStroke(Color.WHITE);
            link.setStrokeWidth(1.6 * s);
            getChildren().add(link);
        }
        for (int i = 0; i < 3; i++) {
            Circle node = new Circle(xs[i] * s, tops[i] * s, 2.6 * s, Color.WHITE);
            getChildren().add(node);
        }
        Circle glow = new Circle(xs[2] * s, tops[2] * s, 2.6 * s, Color.web("#38d6e8"));
        getChildren().add(glow);
    }
}
