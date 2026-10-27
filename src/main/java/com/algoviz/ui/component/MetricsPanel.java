package com.algoviz.ui.component;

import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.MetricType;
import com.algoviz.util.ComplexityUtils;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Grid of metric tiles. Counter tiles come from {@link MetricType} (with their
 * precise definitions as tooltips); extra tiles (step, elapsed time) are added by key.
 */
public final class MetricsPanel extends GridPane {

    private static final int COLUMNS = 2;
    private final Map<MetricType, Label> counterValues = new LinkedHashMap<>();
    private final Map<String, Label> extraValues = new LinkedHashMap<>();
    private int tiles;

    public MetricsPanel() {
        getStyleClass().add("metric-grid");
        for (int i = 0; i < COLUMNS; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100.0 / COLUMNS);
            c.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(c);
        }
    }

    /** Rebuilds the tiles: the listed counters first, then the extra tiles. */
    public void configure(List<MetricType> counters, List<String[]> extras) {
        getChildren().clear();
        counterValues.clear();
        extraValues.clear();
        tiles = 0;
        for (MetricType t : counters) {
            counterValues.put(t, addTile(t.label(), t.description(), false));
        }
        for (String[] e : extras) {
            extraValues.put(e[0], addTile(e[0], e[1], true));
        }
    }

    public void show(ExecutionMetrics metrics) {
        counterValues.forEach((type, label) ->
                label.setText(metrics == null || !metrics.has(type) ? "—" : ComplexityUtils.formatCount(metrics.get(type))));
    }

    public void setExtra(String key, String value) {
        Label l = extraValues.get(key);
        if (l != null) {
            l.setText(value);
        }
    }

    private Label addTile(String name, String description, boolean accent) {
        Label title = new Label(name);
        title.getStyleClass().add("metric-label");
        Label value = new Label("—");
        value.getStyleClass().add("metric-value");
        if (accent) {
            value.getStyleClass().add("accent");
        }
        VBox tile = new VBox(title, value);
        tile.getStyleClass().add("metric-tile");
        tile.setMaxWidth(Double.MAX_VALUE);
        Tooltip tip = new Tooltip(description);
        tip.setShowDelay(Duration.millis(250));
        tip.setWrapText(true);
        Tooltip.install(tile, tip);
        add(tile, tiles % COLUMNS, tiles / COLUMNS);
        tiles++;
        return value;
    }
}
