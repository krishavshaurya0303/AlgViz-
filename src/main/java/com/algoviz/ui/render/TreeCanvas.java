package com.algoviz.ui.render;

import com.algoviz.engine.tree.TreeHighlight;
import com.algoviz.engine.tree.TreeSnapshot;
import com.algoviz.engine.tree.TreeStep;
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
import java.util.List;
import java.util.Map;

/**
 * Draws a BST snapshot. Layout: x = inorder rank (so no two nodes ever overlap
 * and the picture reads left-to-right in sorted order), y = depth.
 * Between steps nodes slide to their new positions by id, new nodes fade in
 * and removed nodes fade out. Nodes can be dragged; their links pull the
 * neighbouring nodes along and everything springs back on release.
 */
public final class TreeCanvas extends ResizableCanvas {

    private static final double MIN_SPACING = 34;
    private static final double MAX_SPACING = 72;
    private static final double PAD = 30;

    private record Pos(double x, double y) {
    }

    private record Placed(TreeSnapshot.Node node, int rank, int depth, TreeSnapshot.Node parent) {
    }

    private final Tween tween = new Tween(this::redraw);
    private final SpringField springs = new SpringField(this::redraw);
    private TreeStep from;
    private TreeStep to;

    public TreeCanvas() {
        enableDragging(springs);
    }

    public void show(TreeStep previous, TreeStep current, double millis) {
        from = previous;
        to = current;
        updateLinks();
        updateMinSize();
        tween.start(previous == null ? 0 : millis);
    }

    public void clear() {
        from = to = null;
        springs.reset();
        updateMinSize();
        redraw();
    }

    private void updateLinks() {
        Map<Integer, Placed> placed = to == null ? Map.of() : place(to.tree());
        List<int[]> links = new ArrayList<>();
        for (Placed p : placed.values()) {
            if (p.parent() != null) {
                links.add(new int[]{p.parent().id(), p.node().id()});
            }
        }
        springs.setLinks(placed.keySet(), links);
    }

    private void updateMinSize() {
        int n = to == null ? 0 : to.tree().size();
        int height = to == null ? 0 : Math.max(0, to.tree().height());
        setMinWidth(n * MIN_SPACING + 2 * PAD);
        setMinHeight((height + 1) * 58 + 2 * PAD);
    }

    private static Map<Integer, Placed> place(TreeSnapshot snap) {
        Map<Integer, Placed> out = new HashMap<>();
        int[] rank = {0};
        walk(snap.root(), null, 0, rank, out);
        return out;
    }

    private static void walk(TreeSnapshot.Node n, TreeSnapshot.Node parent, int depth, int[] rank,
                             Map<Integer, Placed> out) {
        if (n == null) {
            return;
        }
        walk(n.left(), n, depth + 1, rank, out);
        out.put(n.id(), new Placed(n, rank[0]++, depth, parent));
        walk(n.right(), n, depth + 1, rank, out);
    }

    private Map<Integer, Pos> pixels(TreeSnapshot snap, Map<Integer, Placed> placed, double w, double h) {
        Map<Integer, Pos> out = new HashMap<>();
        int n = snap.size();
        if (n == 0) {
            return out;
        }
        double spacing = Math.max(MIN_SPACING, Math.min(MAX_SPACING, (w - 2 * PAD) / n));
        double totalW = spacing * n;
        double left = Math.max(PAD, (w - totalW) / 2);
        int height = Math.max(1, snap.height());
        double level = Math.max(58, Math.min(92, (h - 2 * PAD - 20) / height));
        for (Placed p : placed.values()) {
            out.put(p.node().id(), new Pos(left + p.rank() * spacing + spacing / 2, PAD + 18 + p.depth() * level));
        }
        return out;
    }

    @Override
    protected void draw(GraphicsContext gc, double w, double h) {
        if (to == null || to.tree().isEmpty() && (from == null || from.tree().isEmpty())) {
            return;
        }
        double t = tween.eased();
        Map<Integer, Placed> toPlaced = place(to.tree());
        Map<Integer, Pos> toPos = pixels(to.tree(), toPlaced, w, h);
        Map<Integer, Placed> fromPlaced = from == null ? Map.of() : place(from.tree());
        Map<Integer, Pos> fromPos = from == null ? Map.of() : pixels(from.tree(), fromPlaced, w, h);
        int n = Math.max(1, to.tree().size());
        double spacing = Math.max(MIN_SPACING, Math.min(MAX_SPACING, (w - 2 * PAD) / n));
        double r = Math.max(12, Math.min(19, spacing * 0.40));

        Map<Integer, Pos> current = new HashMap<>();
        for (Map.Entry<Integer, Pos> e : toPos.entrySet()) {
            Pos start = fromPos.get(e.getKey());
            Pos end = e.getValue();
            current.put(e.getKey(), start == null ? end
                    : new Pos(Tween.lerp(start.x(), end.x(), t), Tween.lerp(start.y(), end.y(), t)));
        }
        beginHomes(1, r + 4, r * 1.3);
        current.replaceAll((id, p) -> {
            rememberHome(id, p.x(), p.y());
            return new Pos(p.x() + springs.dx(id), p.y() + springs.dy(id));
        });

        // Edges of nodes that are disappearing fade out.
        for (Placed p : fromPlaced.values()) {
            if (!toPlaced.containsKey(p.node().id()) && p.parent() != null) {
                Pos a = fromPos.get(p.parent().id());
                Pos b = fromPos.get(p.node().id());
                gc.setStroke(Palette.withAlpha(Palette.CORAL, 0.6 * (1 - t)));
                gc.setLineWidth(2);
                gc.strokeLine(a.x(), a.y(), b.x(), b.y());
            }
        }
        // Current edges.
        for (Placed p : toPlaced.values()) {
            if (p.parent() == null) {
                continue;
            }
            Pos a = current.get(p.parent().id());
            Pos b = current.get(p.node().id());
            TreeHighlight hp = to.highlightOf(p.parent().id());
            TreeHighlight hc = to.highlightOf(p.node().id());
            boolean onPath = hp != null && hc != null && hp != TreeHighlight.VISITED;
            boolean isNew = !fromPlaced.containsKey(p.node().id()) && from != null;
            double alpha = isNew ? t : 1;
            double hv = Math.max(hoverOf(p.parent().id()), hoverOf(p.node().id()));
            Color edge = onPath ? Palette.INDIGO : Palette.BORDER_STRONG.interpolate(Palette.TEXT_2, hv);
            gc.setStroke(Palette.withAlpha(edge, alpha));
            gc.setLineWidth((onPath ? 2.4 : 1.6) + hv * 0.6);
            double dx = b.x() - a.x();
            double dy = b.y() - a.y();
            double len = Math.max(1, Math.hypot(dx, dy));
            gc.strokeLine(a.x() + dx / len * r, a.y() + dy / len * r, b.x() - dx / len * r, b.y() - dy / len * r);
        }

        // Removed nodes fade out where they were.
        for (Placed p : fromPlaced.values()) {
            if (!toPlaced.containsKey(p.node().id())) {
                Pos pos = fromPos.get(p.node().id());
                drawNode(gc, pos.x(), pos.y(), r * (1 + 0.3 * t), String.valueOf(p.node().value()),
                        TreeHighlight.DELETE_TARGET, 1 - t);
            }
        }
        List<TreeSnapshot.Node> nodes = to.tree().inorder();
        for (TreeSnapshot.Node node : nodes) {
            Pos pos = current.get(node.id());
            boolean isNew = from != null && !fromPlaced.containsKey(node.id());
            double scale = isNew ? 0.4 + 0.6 * t : 1;
            double hv = hoverOf(node.id());
            double nodeR = r * scale * (1 + 0.14 * hv);
            drawNode(gc, pos.x(), pos.y(), nodeR, String.valueOf(node.value()), to.highlightOf(node.id()),
                    isNew ? t : 1);
            if (hv > 0) {
                gc.setStroke(Palette.withAlpha(Palette.TEXT, 0.55 * hv));
                gc.setLineWidth(1);
                gc.strokeOval(pos.x() - nodeR - 4, pos.y() - nodeR - 4, 2 * nodeR + 8, 2 * nodeR + 8);
            }
        }
        // Mark the root.
        Pos rootPos = to.tree().isEmpty() ? null : current.get(to.tree().root().id());
        if (rootPos != null) {
            gc.setFont(FontLoader.ui(10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.BOTTOM);
            gc.setFill(Palette.TEXT_3);
            gc.fillText("root", rootPos.x(), rootPos.y() - r - 4);
        }
    }

    private void drawNode(GraphicsContext gc, double x, double y, double r, String label, TreeHighlight h,
                          double alpha) {
        Color fill = Palette.BG_3;
        Color stroke = Palette.NEUTRAL_LIGHT;
        Color text = Palette.TEXT;
        if (h != null) {
            switch (h) {
                case CURRENT -> {
                    stroke = Palette.CYAN;
                    fill = Palette.tint(Palette.CYAN);
                }
                case PATH -> {
                    stroke = Palette.INDIGO;
                    fill = Palette.tint(Palette.INDIGO);
                }
                case FOUND, INSERTED -> {
                    stroke = Palette.GREEN;
                    fill = Palette.tint(Palette.GREEN);
                }
                case DELETE_TARGET -> {
                    stroke = Palette.CORAL;
                    fill = Palette.tint(Palette.CORAL);
                }
                case SUCCESSOR -> {
                    stroke = Palette.AMBER;
                    fill = Palette.tint(Palette.AMBER);
                }
                case VISITED -> {
                    stroke = Palette.withAlpha(Palette.GREEN, 0.8);
                    fill = Palette.BG_3.interpolate(Palette.GREEN, 0.12);
                    text = Palette.GREEN;
                }
            }
        }
        gc.setFill(Palette.withAlpha(fill, alpha));
        gc.fillOval(x - r, y - r, 2 * r, 2 * r);
        gc.setStroke(Palette.withAlpha(stroke, alpha));
        gc.setLineWidth(2);
        gc.strokeOval(x - r, y - r, 2 * r, 2 * r);
        gc.setFill(Palette.withAlpha(text, alpha));
        gc.setFont(FontLoader.mono(Math.max(9, Math.min(13, r * 0.72))));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(label, x, y + 0.5);
    }
}
