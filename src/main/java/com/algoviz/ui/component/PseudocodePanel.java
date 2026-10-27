package com.algoviz.ui.component;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Numbered pseudocode listing with the current logical line highlighted. */
public final class PseudocodePanel extends VBox {

    private final List<HBox> lines = new ArrayList<>();
    private int current = -1;

    public PseudocodePanel() {
        getStyleClass().add("pseudocode");
    }

    public void setCode(List<String> code) {
        getChildren().clear();
        lines.clear();
        current = -1;
        for (int i = 0; i < code.size(); i++) {
            Label no = new Label(String.valueOf(i + 1));
            no.getStyleClass().add("line-no");
            Label text = new Label(code.get(i));
            text.getStyleClass().add("code-text");
            text.setWrapText(true);
            text.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
            HBox row = new HBox(no, text);
            row.getStyleClass().add("code-line");
            lines.add(row);
            getChildren().add(row);
        }
    }

    /** Highlights a zero-based line; -1 clears the highlight. */
    public void highlight(int line) {
        if (line == current) {
            return;
        }
        if (current >= 0 && current < lines.size()) {
            lines.get(current).getStyleClass().remove("current");
        }
        current = line;
        if (line >= 0 && line < lines.size()) {
            lines.get(line).getStyleClass().add("current");
        }
    }
}
