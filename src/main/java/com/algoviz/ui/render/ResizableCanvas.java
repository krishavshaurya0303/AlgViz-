package com.algoviz.ui.render;

import com.algoviz.ui.animation.SpringField;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;

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

    // ------------------------------------------------------- draggable nodes

    private SpringField springs;
    /** Layout ("home") position of every node drawn in the last frame, in canvas pixels. */
    private final Map<Integer, double[]> homes = new HashMap<>();
    private double homeScale = 1;
    private double hitRadius = 20;
    private double minDistance;
    private double grabOffX;
    private double grabOffY;

    /**
     * Lets the user grab nodes and drag them around; linked nodes follow through
     * {@link SpringField} and everything springs back on release. Subclasses call
     * {@link #rememberHome} for every node they draw and add {@code springs.dx/dy}
     * (in layout units) to the node's position.
     */
    protected final void enableDragging(SpringField field) {
        this.springs = field;
        setOnMouseMoved(e -> setCursor(nodeAt(e.getX(), e.getY()) != null ? Cursor.OPEN_HAND : Cursor.DEFAULT));
        setOnMouseExited(e -> {
            if (!springs.isGrabbing()) {
                setCursor(Cursor.DEFAULT);
            }
        });
        setOnMousePressed(e -> {
            Integer id = e.getButton() == MouseButton.PRIMARY ? nodeAt(e.getX(), e.getY()) : null;
            if (id == null) {
                return;
            }
            double[] home = homes.get(id);
            grabOffX = e.getX() - (home[0] + springs.dx(id) * homeScale);
            grabOffY = e.getY() - (home[1] + springs.dy(id) * homeScale);
            springs.grab(id, springs.dx(id), springs.dy(id));
            grabbedId = id;
            setCursor(Cursor.CLOSED_HAND);
            e.consume();
        });
        setOnMouseDragged(e -> {
            if (!springs.isGrabbing() || grabbedId == null || !homes.containsKey(grabbedId)) {
                return;
            }
            double[] home = homes.get(grabbedId);
            double x = Math.max(0, Math.min(getWidth(), e.getX() - grabOffX));
            double y = Math.max(0, Math.min(getHeight(), e.getY() - grabOffY));
            springs.drag((x - home[0]) / homeScale, (y - home[1]) / homeScale);
            e.consume();
        });
        setOnMouseReleased(e -> {
            if (springs.isGrabbing()) {
                springs.release();
                grabbedId = null;
                setCursor(nodeAt(e.getX(), e.getY()) != null ? Cursor.OPEN_HAND : Cursor.DEFAULT);
            }
        });
    }

    private Integer grabbedId;

    /** Called at the start of each frame by subclasses that support dragging. */
    protected final void beginHomes(double scale, double radius, double bodyRadius) {
        homes.clear();
        homeScale = scale;
        hitRadius = radius;
        minDistance = 2 * bodyRadius / scale;
    }

    /** Records where node {@code id} sits in the layout, in canvas pixels (before its displacement). */
    protected final void rememberHome(int id, double x, double y) {
        homes.put(id, new double[]{x, y});
        if (springs != null) {
            springs.home(id, x / homeScale, y / homeScale, minDistance);
        }
    }

    private Integer nodeAt(double x, double y) {
        if (springs == null) {
            return null;
        }
        Integer best = null;
        double bestDist = hitRadius;
        for (Map.Entry<Integer, double[]> e : homes.entrySet()) {
            double nx = e.getValue()[0] + springs.dx(e.getKey()) * homeScale;
            double ny = e.getValue()[1] + springs.dy(e.getKey()) * homeScale;
            double d = Math.hypot(x - nx, y - ny);
            if (d <= bestDist) {
                bestDist = d;
                best = e.getKey();
            }
        }
        return best;
    }

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
