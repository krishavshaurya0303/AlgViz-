package com.algoviz.ui.render;

import com.algoviz.engine.linkedlist.ListHighlight;
import com.algoviz.engine.linkedlist.ListSnapshot;
import com.algoviz.engine.linkedlist.ListStep;
import com.algoviz.ui.animation.SpringField;
import com.algoviz.ui.animation.Tween;
import com.algoviz.ui.theme.FontLoader;
import com.algoviz.ui.theme.Palette;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Draws the real pointer structure of a {@link ListSnapshot}: each node is a
 * value box plus a next-pointer box, and every arrow is drawn from the actual
 * next id — so during reversal the arrows visibly turn around. A slash in the
 * next box means null. Nodes not yet (or no longer) linked are drawn raised.
 * Nodes can be dragged; the next pointers pull their neighbours along and
 * everything springs back on release.
 */
public final class LinkedListCanvas extends ResizableCanvas {

    private static final double NODE_W = 86;
    private static final double VALUE_W = 54;
    private static final double NODE_H = 46;
    private static final double GAP = 42;
    private static final double HEAD_W = 58;
    private static final double PAD = 28;
    private static final double RAISE = 92;
    private static final double MIN_SCALE = 0.62;

    private record Pos(double x, double y) {
    }

    private final Tween tween = new Tween(this::redraw);
    private final SpringField springs = new SpringField(this::redraw);
    private ListStep from;
    private ListStep to;

    public LinkedListCanvas() {
        enableDragging(springs);
    }

    public void show(ListStep previous, ListStep current, double millis) {
        from = previous;
        to = current;
        updateLinks();
        int n = current == null ? 0 : current.snapshot().nodes().size();
        setMinWidth(neededWidth(n) * MIN_SCALE);
        tween.start(previous == null ? 0 : millis);
    }

    private void updateLinks() {
        Set<Integer> ids = new HashSet<>();
        List<int[]> links = new ArrayList<>();
        if (to != null) {
            for (ListSnapshot.NodeView n : to.snapshot().nodes()) {
                ids.add(n.id());
                if (n.nextId() != -1) {
                    links.add(new int[]{n.id(), n.nextId()});
                }
            }
        }
        springs.setLinks(ids, links);
    }

    private static double neededWidth(int n) {
        return 2 * PAD + HEAD_W + GAP + n * (NODE_W + GAP) + 60;
    }

    private Map<Integer, Pos> layout(ListSnapshot s, double rowY) {
        Map<Integer, Pos> out = new HashMap<>();
        double x = PAD + HEAD_W + GAP;
        for (ListSnapshot.NodeView n : s.nodes()) {
            out.put(n.id(), new Pos(x, n.detached() ? rowY - RAISE : rowY));
            x += NODE_W + GAP;
        }
        return out;
    }

    @Override
    protected void draw(GraphicsContext gc, double w, double h) {
        if (to == null) {
            return;
        }
        ListSnapshot s = to.snapshot();
        double needed = neededWidth(s.nodes().size());
        double scale = Math.max(MIN_SCALE, Math.min(1, w / needed));
        double contentW = needed * scale;
        double offsetX = Math.max(0, (w - contentW) / 2);
        gc.translate(offsetX, 0);
        gc.scale(scale, scale);
        double vh = h / scale;
        double rowY = Math.max(RAISE + 40, vh / 2 - NODE_H / 2 + 20);

        double t = tween.eased();
        Map<Integer, Pos> toPos = layout(s, rowY);
        Map<Integer, Pos> fromPos = from == null ? Map.of() : layout(from.snapshot(), rowY);
        Map<Integer, Pos> pos = new HashMap<>();
        toPos.forEach((id, p) -> {
            Pos start = fromPos.get(id);
            pos.put(id, start == null ? p : new Pos(Tween.lerp(start.x(), p.x(), t), Tween.lerp(start.y(), p.y(), t)));
        });
        beginHomes(scale, NODE_W / 2 * scale, NODE_H * scale);
        pos.replaceAll((id, p) -> {
            rememberHome(id, offsetX + (p.x() + NODE_W / 2) * scale, (p.y() + NODE_H / 2) * scale);
            return new Pos(p.x() + springs.dx(id), p.y() + springs.dy(id));
        });

        // head pointer box
        double headX = PAD;
        double headY = rowY + 6;
        gc.setFill(Palette.BG_3);
        gc.fillRoundRect(headX, headY, HEAD_W, NODE_H - 12, 3, 3);
        gc.setStroke(s.headChanged() ? Palette.AMBER : Palette.BORDER_STRONG);
        gc.setLineWidth(1.4);
        gc.strokeRoundRect(headX, headY, HEAD_W, NODE_H - 12, 3, 3);
        gc.setFill(Palette.TEXT);
        gc.setFont(FontLoader.mono(12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("head", headX + HEAD_W / 2, headY + (NODE_H - 12) / 2);
        double hx = headX + HEAD_W;
        double hy = rowY + NODE_H / 2;
        if (s.headId() == -1) {
            gc.setFill(Palette.TEXT_3);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText("→ null", hx + 8, hy);
        } else {
            Pos target = pos.get(s.headId());
            drawArrow(gc, hx, hy, target.x(), target.y() + NODE_H / 2, s.headChanged() ? Palette.AMBER : Palette.TEXT_2,
                    s.headChanged());
        }

        // arrows between nodes
        for (ListSnapshot.NodeView n : s.nodes()) {
            if (n.nextId() == -1) {
                continue;
            }
            Pos a = pos.get(n.id());
            Pos b = pos.get(n.nextId());
            if (a == null || b == null) {
                continue;
            }
            boolean changed = s.changedLink() == n.id();
            double sx = a.x() + VALUE_W + (NODE_W - VALUE_W) / 2;
            double sy = a.y() + NODE_H / 2;
            double hv = Math.max(hoverOf(n.id()), hoverOf(n.nextId()));
            drawArrow(gc, sx, sy, b.x(), b.y() + NODE_H / 2, changed ? Palette.AMBER : Palette.TEXT_2.interpolate(Palette.TEXT, hv),
                    changed || hv > 0.5);
        }

        // nodes
        int last = s.nodes().size() - 1;
        for (int i = 0; i < s.nodes().size(); i++) {
            ListSnapshot.NodeView n = s.nodes().get(i);
            Pos p = pos.get(n.id());
            boolean appearing = from != null && !fromPos.containsKey(n.id());
            double hv = hoverOf(n.id());
            // Hover magnifies the node about its centre.
            double grow = 1 + 0.14 * hv;
            double cx = p.x() + NODE_W / 2;
            double cy = p.y() + NODE_H / 2;
            gc.save();
            gc.translate(cx, cy);
            gc.scale(grow, grow);
            gc.translate(-cx, -cy);
            drawNode(gc, p.x(), p.y(), n, s.highlights().get(n.id()), appearing ? t : 1);
            if (hv > 0) {
                gc.setStroke(Palette.withAlpha(Palette.TEXT, 0.55 * hv));
                gc.setLineWidth(1);
                gc.strokeRoundRect(p.x() - 4, p.y() - 4, NODE_W + 8, NODE_H + 8, 6, 6);
            }
            gc.restore();
            if (i == last && n.nextId() == -1 && !n.detached()) {
                gc.setFill(Palette.TEXT_3);
                gc.setFont(FontLoader.mono(11));
                gc.setTextAlign(TextAlignment.LEFT);
                gc.setTextBaseline(VPos.CENTER);
                gc.fillText("null", p.x() + NODE_W + 10, p.y() + NODE_H / 2);
            }
        }

        // removed nodes fade out
        if (from != null) {
            for (ListSnapshot.NodeView n : from.snapshot().nodes()) {
                if (!toPos.containsKey(n.id())) {
                    Pos p = fromPos.get(n.id());
                    drawNode(gc, p.x(), p.y() - 20 * t, n, ListHighlight.REMOVING, 1 - t);
                }
            }
        }

        drawPointers(gc, s, pos, rowY);
    }

    private void drawNode(GraphicsContext gc, double x, double y, ListSnapshot.NodeView n, ListHighlight h,
                          double alpha) {
        Color stroke = Palette.NEUTRAL_LIGHT;
        Color fill = Palette.BG_3;
        if (h != null) {
            switch (h) {
                case CURRENT -> {
                    stroke = Palette.CYAN;
                    fill = Palette.tint(Palette.CYAN);
                }
                case VISITED -> {
                    stroke = Palette.withAlpha(Palette.BLUE, 0.9);
                    fill = Palette.tint(Palette.BLUE);
                }
                case FOUND -> {
                    stroke = Palette.GREEN;
                    fill = Palette.tint(Palette.GREEN);
                }
                case NEW -> {
                    stroke = Palette.GREEN;
                    fill = Palette.BG_3.interpolate(Palette.GREEN, 0.12);
                }
                case REMOVING -> {
                    stroke = Palette.CORAL;
                    fill = Palette.tint(Palette.CORAL);
                }
            }
        }
        gc.setFill(Palette.withAlpha(fill, alpha));
        gc.fillRoundRect(x, y, NODE_W, NODE_H, 4, 4);
        gc.setFill(Palette.withAlpha(Palette.BG_1, alpha));
        gc.fillRoundRect(x + VALUE_W, y + 1, NODE_W - VALUE_W - 1, NODE_H - 2, 3, 3);
        gc.setStroke(Palette.withAlpha(stroke, alpha));
        gc.setLineWidth(1.8);
        gc.strokeRoundRect(x, y, NODE_W, NODE_H, 4, 4);
        gc.setLineWidth(1);
        gc.strokeLine(x + VALUE_W, y + 4, x + VALUE_W, y + NODE_H - 4);

        gc.setFill(Palette.withAlpha(Palette.TEXT, alpha));
        gc.setFont(FontLoader.mono(15));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(n.value()), x + VALUE_W / 2, y + NODE_H / 2);

        double nx = x + VALUE_W + (NODE_W - VALUE_W) / 2;
        double ny = y + NODE_H / 2;
        if (n.nextId() == -1) {
            gc.setStroke(Palette.withAlpha(Palette.TEXT_3, alpha));
            gc.setLineWidth(1.4);
            gc.strokeLine(x + VALUE_W + 7, y + NODE_H - 9, x + NODE_W - 7, y + 9);
        } else {
            gc.setFill(Palette.withAlpha(Palette.TEXT_2, alpha));
            gc.fillOval(nx - 3, ny - 3, 6, 6);
        }
    }

    /** Arrow from (x1,y1) to the left edge point (x2,y2), curving when not a simple neighbour link. */
    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2, Color color, boolean bold) {
        gc.setStroke(color);
        gc.setFill(color);
        gc.setLineWidth(bold ? 2.4 : 1.6);
        double dx = x2 - x1;
        boolean straight = Math.abs(y2 - y1) < 1 && dx > 0 && dx < NODE_W + GAP;
        if (straight) {
            gc.strokeLine(x1, y1, x2 - 2, y2);
            arrowHead(gc, x1, y1, x2, y2, 8);
            return;
        }
        double c1x;
        double c1y;
        double c2x;
        double c2y;
        double ex = x2;
        double ey = y2;
        double sx = x1;
        double sy = y1;
        if (dx > 0) {
            // forward link that skips a node or changes level: gentle arc above the row
            double lift = Math.abs(y2 - y1) < 1 ? -60 : 0;
            c1x = x1 + Math.max(30, dx * 0.35);
            c1y = y1 + lift;
            c2x = x2 - Math.max(30, dx * 0.35);
            c2y = y2 + lift;
        } else {
            // backward link (e.g. during reversal): leave from the top of the next box,
            // arc over and land on top of the target node
            sy = y1 - NODE_H / 2;
            ex = x2 + VALUE_W / 2;
            ey = y2 - 2;
            double lift = 46 + Math.min(30, -dx * 0.05);
            c1x = x1;
            c1y = sy - lift;
            c2x = ex;
            c2y = ey - lift;
        }
        gc.beginPath();
        gc.moveTo(sx, sy);
        gc.bezierCurveTo(c1x, c1y, c2x, c2y, ex, ey);
        gc.stroke();
        arrowHead(gc, c2x, c2y, ex, ey, 8);
    }

    private void drawPointers(GraphicsContext gc, ListSnapshot s, Map<Integer, Pos> pos, double rowY) {
        Map<Integer, Integer> stack = new HashMap<>();
        // Null pointers are listed above the head box: arrows and raised nodes never reach that corner,
        // and it is always inside the canvas (the space right of the last node is too narrow).
        double nullX = PAD;
        int nullRow = 0;
        gc.setFont(FontLoader.mono(11));
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        for (Map.Entry<String, Integer> e : s.pointers().entrySet()) {
            String name = e.getKey();
            Color c = switch (name) {
                case "current" -> Palette.CYAN;
                case "prev" -> Palette.VIOLET;
                case "next" -> Palette.AMBER;
                case "new" -> Palette.GREEN;
                case "removed" -> Palette.CORAL;
                default -> Palette.TEXT_2;
            };
            double tw = name.length() * 7 + 16;
            if (e.getValue() == -1) {
                String text = name + " = null";
                double w2 = text.length() * 7 + 16;
                double y = rowY - 22 - nullRow * 24;
                gc.setFill(Palette.withAlpha(c, 0.15));
                gc.fillRoundRect(nullX, y - 10, w2, 20, 3, 3);
                gc.setFill(ink(c));
                gc.fillText(text, nullX + w2 / 2, y);
                nullRow++;
                continue;
            }
            Pos p = pos.get(e.getValue());
            if (p == null) {
                continue;
            }
            int level = stack.merge(e.getValue(), 1, Integer::sum) - 1;
            double cx = p.x() + VALUE_W / 2;
            double top = p.y() + NODE_H + 30 + level * 24;
            gc.setStroke(c);
            gc.setFill(c);
            gc.setLineWidth(1.4);
            if (level == 0) {
                gc.strokeLine(cx, top - 10, cx, p.y() + NODE_H + 6);
                arrowHead(gc, cx, top - 10, cx, p.y() + NODE_H + 4, 6);
            }
            gc.setFill(Palette.withAlpha(c, 0.15));
            gc.fillRoundRect(cx - tw / 2, top - 10, tw, 20, 3, 3);
            gc.setFill(ink(c));
            gc.fillText(name, cx, top);
        }
    }

    /** A role colour pulled towards the ink colour, so pointer labels stay readable on their pale tint. */
    private static Color ink(Color c) {
        return c.interpolate(Palette.TEXT, 0.65);
    }
}
