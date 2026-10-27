package com.algoviz.ui.component;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/** Factory for icon nodes. Colour comes from CSS (.icon-stroke / .icon-fill). */
public final class Icons {

    private Icons() {
    }

    public static Node create(IconType type, double size) {
        SVGPath p = new SVGPath();
        p.setContent(type.path());
        p.getStyleClass().add(type.filled() ? "icon-fill" : "icon-stroke");
        if (!type.filled()) {
            p.setStrokeWidth(1.9);
        }
        double scale = size / 24.0;
        p.setScaleX(scale);
        p.setScaleY(scale);
        StackPane box = new StackPane(new Group(p));
        box.setMinSize(size, size);
        box.setPrefSize(size, size);
        box.setMaxSize(size, size);
        box.setMouseTransparent(true);
        box.getStyleClass().add("icon");
        return box;
    }
}
