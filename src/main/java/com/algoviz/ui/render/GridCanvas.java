package com.algoviz.ui.render;

import com.algoviz.engine.graph.GraphStep;
import com.algoviz.engine.graph.GridGraph;
import com.algoviz.engine.graph.NodeVisual;
import com.algoviz.ui.animation.Tween;
import com.algoviz.ui.theme.FontLoader;
import com.algoviz.ui.theme.Palette;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Draws the editable grid and the search state of the current step.
 * Cells whose state changed since the previous step "pop" in during the
 * transition. Mouse presses and drags are reported as cell coordinates.
 */
public final class GridCanvas extends ResizableCanvas {

    /** Receives cell interactions. */
    public interface CellListener {
        /**
         * @param dragging false for the initial press, true while the mouse is dragged
         */
        void onCell(int row, int col, MouseButton button, boolean dragging);
    }

    private final Tween tween = new Tween(this::redraw);
    private GridGraph grid;
    private GraphStep from;
    private GraphStep to;
    private boolean showDistances = true;
    private CellListener listener;
    private int hoverRow = -1;
    private int hoverCol = -1;
    private int lastRow = -1;
    private int lastCol = -1;
    private MouseButton activeButton;
    private boolean editable = true;

    // geometry of the last draw
    private double cell;
    private double originX;
    private double originY;

    public GridCanvas() {
        setOnMousePressed(this::onPressed);
        setOnMouseDragged(this::onDragged);
        setOnMouseReleased(e -> activeButton = null);
        setOnMouseMoved(this::onMoved);
        setOnMouseExited(e -> {
            hoverRow = hoverCol = -1;
            redraw();
        });
    }

    public void setGrid(GridGraph grid) {
        this.grid = grid;
        redraw();
    }

    public void setCellListener(CellListener listener) {
        this.listener = listener;
    }

    public void setShowDistances(boolean value) {
        showDistances = value;
        redraw();
    }

    public void setEditable(boolean value) {
        editable = value;
        setCursor(value ? javafx.scene.Cursor.CROSSHAIR : javafx.scene.Cursor.DEFAULT);
    }

    public void show(GraphStep previous, GraphStep current, double millis) {
        from = previous;
        to = current;
        tween.start(previous == null || current == null ? 0 : millis);
    }

    @Override
    protected void draw(GraphicsContext gc, double w, double h) {
        if (grid == null) {
            return;
        }
        int rows = grid.rows();
        int cols = grid.cols();
        double top = 52;
        double bottom = 48;
        cell = Math.floor(Math.min((w - 24) / cols, (h - top - bottom) / rows));
        cell = Math.max(6, cell);
        originX = Math.floor((w - cell * cols) / 2);
        originY = Math.floor(top + (h - top - bottom - cell * rows) / 2);
        double t = tween.eased();
        double gap = cell >= 14 ? 1.5 : 1;
        double radius = Math.min(5, cell * 0.22);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int node = grid.index(r, c);
                double x = originX + c * cell;
                double y = originY + r * cell;
                NodeVisual vis = to == null ? NodeVisual.UNSEEN : to.stateOf(node);
                boolean changed = from != null && to != null && from.stateOf(node) != vis;
                double inset = gap + (changed ? (1 - t) * cell * 0.32 : 0);

                // base
                if (grid.isWall(node)) {
                    gc.setFill(Palette.CELL_WALL);
                    gc.fillRoundRect(x + gap, y + gap, cell - 2 * gap, cell - 2 * gap, radius, radius);
                    gc.setFill(Palette.CELL_WALL_TOP);
                    gc.fillRoundRect(x + gap, y + gap, cell - 2 * gap, Math.max(2, cell * 0.18), radius, radius);
                    continue;
                }
                gc.setFill(grid.isWeighted(node) ? Palette.CELL_WEIGHT : Palette.CELL_EMPTY);
                gc.fillRoundRect(x + gap, y + gap, cell - 2 * gap, cell - 2 * gap, radius, radius);

                Color overlay = switch (vis) {
                    case FRONTIER -> Palette.CELL_FRONTIER;
                    case VISITED -> grid.isWeighted(node) ? Color.web("#4a3f6e") : Palette.CELL_VISITED;
                    case CURRENT -> Palette.AMBER;
                    case PATH -> Palette.CELL_PATH;
                    default -> null;
                };
                if (overlay != null) {
                    gc.setFill(overlay);
                    gc.fillRoundRect(x + inset, y + inset, cell - 2 * inset, cell - 2 * inset, radius, radius);
                }
                if (grid.isWeighted(node)) {
                    drawWeight(gc, x, y, vis);
                }
                if (showDistances && to != null && to.hasDistances() && cell >= 20
                        && node != grid.start() && node != grid.target()
                        && to.distance(node) != Integer.MAX_VALUE && vis != NodeVisual.PATH) {
                    gc.setFont(FontLoader.mono(Math.min(11, cell * 0.38)));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.setTextBaseline(VPos.CENTER);
                    gc.setFill(vis == NodeVisual.CURRENT ? Palette.BG_0 : Palette.withAlpha(Palette.TEXT, 0.8));
                    gc.fillText(String.valueOf(to.distance(node)), x + cell / 2, y + cell / 2 + 0.5);
                }
            }
        }
        drawEndpoint(gc, grid.start(), Palette.INDIGO, "S");
        drawEndpoint(gc, grid.target(), Palette.CORAL, "T");

        if (editable && hoverRow >= 0 && grid.inBounds(hoverRow, hoverCol)) {
            gc.setStroke(Palette.withAlpha(Palette.CYAN, 0.8));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(originX + hoverCol * cell + 1, originY + hoverRow * cell + 1,
                    cell - 2, cell - 2, radius, radius);
        }
    }

    private void drawWeight(GraphicsContext gc, double x, double y, NodeVisual vis) {
        if (cell < 12) {
            return;
        }
        gc.setFill(vis == NodeVisual.PATH || vis == NodeVisual.CURRENT ? Palette.BG_0
                : Palette.withAlpha(Palette.AMBER, 0.85));
        gc.setFont(FontLoader.mono(Math.min(10, cell * 0.32)));
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText("×" + grid.weightedCost(), x + cell - 3, y + 2);
    }

    private void drawEndpoint(GraphicsContext gc, int node, Color color, String letter) {
        double x = originX + grid.colOf(node) * cell;
        double y = originY + grid.rowOf(node) * cell;
        double r = cell * 0.38;
        double cx = x + cell / 2;
        double cy = y + cell / 2;
        gc.setFill(Palette.withAlpha(color, 0.25));
        gc.fillOval(cx - r - 3, cy - r - 3, 2 * r + 6, 2 * r + 6);
        gc.setFill(color);
        gc.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        if (cell >= 14) {
            gc.setFill(Color.WHITE);
            gc.setFont(FontLoader.uiBold(Math.min(12, cell * 0.42)));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(letter, cx, cy + 0.5);
        }
    }

    // ------------------------------------------------------------ mouse

    private int[] cellAt(double px, double py) {
        if (grid == null || cell <= 0) {
            return null;
        }
        int c = (int) Math.floor((px - originX) / cell);
        int r = (int) Math.floor((py - originY) / cell);
        return grid.inBounds(r, c) ? new int[]{r, c} : null;
    }

    private void onPressed(MouseEvent e) {
        requestFocus();
        int[] rc = cellAt(e.getX(), e.getY());
        if (rc == null || listener == null || !editable) {
            return;
        }
        activeButton = e.getButton();
        lastRow = rc[0];
        lastCol = rc[1];
        listener.onCell(rc[0], rc[1], e.getButton(), false);
    }

    private void onDragged(MouseEvent e) {
        int[] rc = cellAt(e.getX(), e.getY());
        updateHover(rc);
        if (rc == null || listener == null || !editable || activeButton == null) {
            return;
        }
        if (rc[0] != lastRow || rc[1] != lastCol) {
            lastRow = rc[0];
            lastCol = rc[1];
            listener.onCell(rc[0], rc[1], activeButton, true);
        }
    }

    private void onMoved(MouseEvent e) {
        updateHover(cellAt(e.getX(), e.getY()));
    }

    private void updateHover(int[] rc) {
        int r = rc == null ? -1 : rc[0];
        int c = rc == null ? -1 : rc[1];
        if (r != hoverRow || c != hoverCol) {
            hoverRow = r;
            hoverCol = c;
            redraw();
        }
    }
}
