package com.algoviz.ui.render;

import com.algoviz.engine.sorting.MarkerRole;
import com.algoviz.engine.sorting.SortEventType;
import com.algoviz.engine.sorting.SortMarker;
import com.algoviz.engine.sorting.SortStep;
import com.algoviz.ui.animation.Tween;
import com.algoviz.ui.theme.FontLoader;
import com.algoviz.ui.theme.Palette;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Draws the array as vertical bars. Transitions are derived from the
 * difference between the previous and the current snapshot: an exchange of two
 * values slides the bars past each other, any other change animates the
 * height. Because animation only interpolates between two recorded states it
 * works identically when stepping backward.
 */
public final class SortingCanvas extends ResizableCanvas {

    private static final double PAD_X = 22;
    private static final double PAD_TOP = 64;
    private static final double KEY_BAND = 44;
    private static final double LABEL_BAND = 64;
    private static final double BUFFER_FRACTION = 0.24;

    private final Tween tween = new Tween(this::redraw);
    private SortStep from;
    private SortStep to;
    private int minValue = 0;
    private int maxValue = 100;
    private boolean reserveBuffer;
    private boolean reserveKeyBand;

    public void setValueRange(int min, int max) {
        this.minValue = Math.min(0, min);
        this.maxValue = Math.max(Math.max(0, max), this.minValue + 1);
    }

    /** Reserve space below the bars for Merge Sort's buffer. */
    public void setReserveBuffer(boolean value) {
        reserveBuffer = value;
        redraw();
    }

    /** Reserve space above the bars for Insertion Sort's lifted key. */
    public void setReserveKeyBand(boolean value) {
        reserveKeyBand = value;
        redraw();
    }

    public void show(SortStep previous, SortStep current, double millis) {
        boolean animate = previous != null && current != null && previous.size() == current.size();
        from = animate ? previous : current;
        to = current;
        tween.start(animate ? millis : 0);
    }

    public void clear() {
        from = to = null;
        tween.finish();
        redraw();
    }

    @Override
    protected void draw(GraphicsContext gc, double w, double h) {
        if (to == null || to.size() == 0) {
            return;
        }
        int n = to.size();
        double t = tween.eased();
        double top = PAD_TOP + (reserveKeyBand ? KEY_BAND : 0);
        double bufferH = reserveBuffer ? Math.max(60, h * BUFFER_FRACTION) : 0;
        double chartBottom = h - LABEL_BAND - bufferH - (reserveBuffer ? 14 : 0);
        double chartH = Math.max(20, chartBottom - top);
        double slot = (w - 2 * PAD_X) / n;
        double barW = Math.max(1, slot * (n > 60 ? 0.8 : 0.7));
        double range = maxValue - minValue;
        double zeroY = chartBottom - (-minValue) / range * chartH;

        // Active range background
        if (to.hasRange()) {
            double x0 = PAD_X + to.rangeLo() * slot;
            double x1 = PAD_X + (to.rangeHi() + 1) * slot;
            gc.setFill(Palette.withAlpha(Palette.TEXT, 0.035));
            gc.fillRoundRect(x0, top - 10, x1 - x0, chartBottom - top + 10, 3, 3);
            gc.setStroke(Palette.withAlpha(Palette.TEXT_3, 0.6));
            gc.setLineWidth(1);
            gc.strokeLine(x0 + 4, top - 10, x1 - 4, top - 10);
        }

        // Baseline
        gc.setStroke(Palette.BORDER_STRONG);
        gc.setLineWidth(1);
        gc.strokeLine(PAD_X - 6, zeroY + 0.5, w - PAD_X + 6, zeroY + 0.5);

        int swapA = -1;
        int swapB = -1;
        if (from != to && from != null) {
            int diffs = 0;
            int a = -1;
            int b = -1;
            for (int i = 0; i < n && diffs <= 2; i++) {
                if (from.valueAt(i) != to.valueAt(i)) {
                    if (diffs == 0) {
                        a = i;
                    } else {
                        b = i;
                    }
                    diffs++;
                }
            }
            if (diffs == 2 && from.valueAt(a) == to.valueAt(b) && from.valueAt(b) == to.valueAt(a)) {
                swapA = a;
                swapB = b;
            }
        }

        Font valueFont = FontLoader.mono(Math.max(8, Math.min(12, barW * 0.48)));
        boolean showValues = barW >= 15;
        boolean showIndices = slot >= 19;

        for (int i = 0; i < n; i++) {
            double value;
            double x = PAD_X + i * slot + (slot - barW) / 2;
            if (i == swapA || i == swapB) {
                int other = i == swapA ? swapB : swapA;
                // the bar now at i came from position "other"
                double fromX = PAD_X + other * slot + (slot - barW) / 2;
                x = Tween.lerp(fromX, x, t);
                value = to.valueAt(i);
            } else {
                value = from == null ? to.valueAt(i) : Tween.lerp(from.valueAt(i), to.valueAt(i), t);
            }
            double barTop = value >= 0 ? zeroY - value / range * chartH : zeroY;
            double barH = Math.max(2, Math.abs(value) / range * chartH);
            Color fill = colorFor(i);
            boolean ghost = isGap(i) && to.isHolding();
            boolean dim = to.hasRange() && (i < to.rangeLo() || i > to.rangeHi()) && !to.isSorted(i);
            double alpha = dim ? 0.38 : 1;
            double radius = Math.min(2, barW * 0.2);
            if (ghost) {
                gc.setLineDashes(4, 3);
                gc.setStroke(Palette.withAlpha(Palette.CYAN, 0.8));
                gc.setLineWidth(1.4);
                gc.strokeRoundRect(x + 0.7, barTop + 0.7, barW - 1.4, barH - 1.4, radius, radius);
                gc.setLineDashes((double[]) null);
                gc.setFill(Palette.withAlpha(Palette.CYAN, 0.08));
                gc.fillRoundRect(x, barTop, barW, barH, radius, radius);
            } else {
                gc.setFill(Palette.withAlpha(fill, alpha));
                gc.fillRoundRect(x, barTop, barW, barH, radius, radius);
            }
            if (showValues) {
                gc.setFont(valueFont);
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(value >= 0 ? VPos.BOTTOM : VPos.TOP);
                gc.setFill(Palette.withAlpha(ghost ? Palette.TEXT_3 : Palette.TEXT_2, alpha));
                double ty = value >= 0 ? barTop - 4 : barTop + barH + 4;
                gc.fillText(String.valueOf((int) Math.round(value)), x + barW / 2, ty);
            }
        }

        double labelY = chartBottom + 8;
        if (showIndices) {
            gc.setFont(FontLoader.mono(10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.TOP);
            gc.setFill(Palette.TEXT_3);
            int stride = slot >= 19 ? 1 : 5;
            for (int i = 0; i < n; i += stride) {
                gc.fillText(String.valueOf(i), PAD_X + i * slot + slot / 2, labelY);
            }
        }
        drawMarkers(gc, slot, labelY + 24);
        if (to.isHolding()) {
            drawHeldKey(gc, slot, top);
        }
        if (reserveBuffer && to.hasBuffer()) {
            drawBuffer(gc, slot, barW, chartBottom + LABEL_BAND + 10, bufferH);
        }
    }

    private boolean isGap(int i) {
        for (SortMarker m : to.markers()) {
            if (m.role() == MarkerRole.GAP && m.index() == i) {
                return true;
            }
        }
        return false;
    }

    private Color colorFor(int i) {
        if (to.type() == SortEventType.DONE) {
            return Palette.GREEN;
        }
        if (i == to.first() || i == to.second()) {
            switch (to.type()) {
                case COMPARE:
                    return Palette.CORAL;
                case SWAP:
                case WRITE:
                    return Palette.AMBER;
                case MARK_SORTED:
                    return Palette.GREEN;
                default:
                    break;
            }
        }
        for (SortMarker m : to.markers()) {
            if (m.index() == i) {
                if (m.role() == MarkerRole.PIVOT) {
                    return Palette.VIOLET;
                }
                if (m.role() == MarkerRole.MINIMUM) {
                    return Palette.CYAN;
                }
            }
        }
        if (to.isSorted(i)) {
            return Palette.withAlpha(Palette.GREEN, 0.78);
        }
        if (to.type() == SortEventType.NOTE && (i == to.first() || i == to.second())) {
            return Palette.NEUTRAL_LIGHT;
        }
        return Palette.NEUTRAL;
    }

    private void drawMarkers(GraphicsContext gc, double slot, double y) {
        gc.setFont(FontLoader.mono(10));
        gc.setTextBaseline(VPos.CENTER);
        gc.setTextAlign(TextAlignment.CENTER);
        java.util.Map<Integer, Integer> stacked = new java.util.HashMap<>();
        for (SortMarker m : to.markers()) {
            String text = m.label();
            double tw = text.length() * 6.2 + 10;
            int level = stacked.merge(m.index(), 1, Integer::sum) - 1;
            double cx = PAD_X + m.index() * slot + slot / 2;
            double cy = y + 6 + level * 18;
            Color c = switch (m.role()) {
                case PIVOT -> Palette.VIOLET;
                case MINIMUM, GAP -> Palette.CYAN;
                default -> Palette.TEXT_2;
            };
            gc.setFill(Palette.withAlpha(c, 0.16));
            gc.fillRoundRect(cx - tw / 2, cy - 8, tw, 16, 8, 8);
            gc.setFill(c);
            gc.fillText(text, cx, cy);
            if (level == 0) {
                gc.fillPolygon(new double[]{cx - 3, cx + 3, cx}, new double[]{cy - 8, cy - 8, cy - 12}, 3);
            }
        }
    }

    private void drawHeldKey(GraphicsContext gc, double slot, double top) {
        int gap = -1;
        for (SortMarker m : to.markers()) {
            if (m.role() == MarkerRole.GAP) {
                gap = m.index();
            }
        }
        if (gap < 0) {
            return;
        }
        String text = "key = " + to.heldValue();
        double tw = text.length() * 7 + 18;
        double cx = PAD_X + gap * slot + slot / 2;
        double cy = top - KEY_BAND / 2 - 4;
        cx = Math.max(PAD_X + tw / 2, Math.min(canvas.getWidth() - PAD_X - tw / 2, cx));
        gc.setFill(Palette.withAlpha(Palette.CYAN, 0.16));
        gc.fillRoundRect(cx - tw / 2, cy - 12, tw, 24, 12, 12);
        gc.setStroke(Palette.CYAN);
        gc.setLineWidth(1.2);
        gc.strokeRoundRect(cx - tw / 2, cy - 12, tw, 24, 12, 12);
        gc.setFill(Palette.CYAN);
        gc.setFont(FontLoader.mono(12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(text, cx, cy);
    }

    private void drawBuffer(GraphicsContext gc, double slot, double barW, double y, double height) {
        int lo = to.bufferLo();
        int len = to.bufferLength();
        int mid = to.bufferMid();
        double range = maxValue - minValue;
        double baseY = y + height - 6;
        double usable = height - 22;
        double zeroY = baseY - (-minValue) / range * usable;

        gc.setFont(FontLoader.ui(10.5));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFill(Palette.TEXT_3);
        gc.fillText("temporary buffer  (left half | right half)", PAD_X, y - 2);

        for (int k = 0; k < len; k++) {
            int idx = lo + k;
            int v = to.bufferValue(k);
            boolean leftSide = idx <= mid;
            boolean consumed = leftSide ? idx < to.bufferLeft() : idx < to.bufferRight();
            boolean head = leftSide ? idx == to.bufferLeft() : idx == to.bufferRight();
            double x = PAD_X + idx * slot + (slot - barW) / 2;
            double bh = Math.max(2, Math.abs(v) / range * usable);
            double by = v >= 0 ? zeroY - bh : zeroY;
            Color c = leftSide ? Palette.BLUE : Palette.VIOLET;
            if (head && to.type() == SortEventType.COMPARE) {
                c = Palette.CORAL;
            }
            gc.setFill(Palette.withAlpha(c, consumed ? 0.16 : 0.75));
            gc.fillRoundRect(x, by, barW, bh, 4, 4);
            if (barW >= 15) {
                gc.setFont(FontLoader.mono(Math.min(11, barW * 0.45)));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.BOTTOM);
                gc.setFill(Palette.withAlpha(Palette.TEXT_2, consumed ? 0.35 : 1));
                gc.fillText(String.valueOf(v), x + barW / 2, by - 2);
            }
            if (head && !consumed) {
                gc.setStroke(Palette.TEXT);
                gc.setLineWidth(1);
                gc.strokeRoundRect(x - 1, by - 1, barW + 2, bh + 2, 5, 5);
            }
        }
        double divX = PAD_X + (mid + 1) * slot;
        gc.setStroke(Palette.TEXT_3);
        gc.setLineDashes(3, 3);
        gc.strokeLine(divX, y + 14, divX, baseY + 2);
        gc.setLineDashes((double[]) null);
    }
}
