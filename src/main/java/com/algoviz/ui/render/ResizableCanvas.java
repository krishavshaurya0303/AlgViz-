package com.algoviz.ui.render;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;

/**
 * Base class for the visualization surfaces: a Canvas that always fills its
 * Region and redraws whenever it is resized or asked to. Subclasses only implement
 * {@link #draw(GraphicsContext, double, double)}.
 */
public abstract class ResizableCanvas extends Region {

    protected final Canvas canvas = new Canvas();

    protected ResizableCanvas() {
        getChildren().add(canvas);
        setMinSize(0, 0);
    }

    @Override
    protected void layoutChildren() {
        double w = snapSizeX(getWidth());
        double h = snapSizeY(getHeight());
        if (canvas.getWidth() != w || canvas.getHeight() != h) {
            canvas.setWidth(w);
            canvas.setHeight(h);
        }
        redraw();
    }

    public final void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        if (w > 2 && h > 2) {
            gc.save();
            draw(gc, w, h);
            gc.restore();
        }
    }

    protected abstract void draw(GraphicsContext gc, double w, double h);

    // -------------------------------------------------------------- helpers

    protected static void arrowHead(GraphicsContext gc, double fromX, double fromY, double tipX, double tipY,
                                    double size) {
        double angle = Math.atan2(tipY - fromY, tipX - fromX);
        double a1 = angle + Math.PI * 0.82;
        double a2 = angle - Math.PI * 0.82;
        gc.fillPolygon(
                new double[]{tipX, tipX + size * Math.cos(a1), tipX + size * Math.cos(a2)},
                new double[]{tipY, tipY + size * Math.sin(a1), tipY + size * Math.sin(a2)}, 3);
    }
}
