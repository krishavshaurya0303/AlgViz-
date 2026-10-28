package com.algoviz.ui.component;

import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.ComplexityEntry;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/** Theoretical (documented, not measured) complexity of the selected algorithm. */
public final class ComplexityCard extends VBox {

    private final GridPane grid = new GridPane();
    private final Label note = new Label();

    public ComplexityCard() {
        getStyleClass().add("card");
        Label title = new Label("Theoretical complexity");
        title.getStyleClass().add("card-title");
        grid.getStyleClass().add("complexity-grid");
        note.getStyleClass().add("note-text");
        getChildren().addAll(title, grid, note);
    }

    public void show(AlgorithmMetadata m) {
        grid.getChildren().clear();
        int row = 0;
        row = add("Best", m.bestTime(), row);
        row = add("Average", m.averageTime(), row);
        row = add("Worst", m.worstTime(), row);
        row = add("Space", m.space(), row);
        if (m.stable() != null) {
            row = add("Stable", m.stable() ? "Yes" : "No", row);
        }
        if (m.inPlace() != null) {
            add("In place", m.inPlace() ? "Yes" : "No", row);
        }
        setNote(m.complexityNote());
    }

    /** Operation table for data structures (Tree / Linked List). */
    public void showOperations(List<ComplexityEntry> entries, String headerA, String headerB, String noteText) {
        grid.getChildren().clear();
        Label h0 = head("Operation");
        Label h1 = head(headerA);
        Label h2 = head(headerB);
        grid.addRow(0, h0, h1, h2);
        int row = 1;
        for (ComplexityEntry e : entries) {
            Label k = new Label(e.operation());
            k.getStyleClass().add("complexity-key");
            grid.addRow(row++, k, value(e.average()), value(e.worst()));
        }
        setNote(noteText);
    }

    /** Single highlighted operation (used by the list lab to show the current operation). */
    public void showSingle(String operation, String time, String space, String noteText) {
        grid.getChildren().clear();
        int row = add("Operation", operation, 0);
        row = add("Time", time, row);
        add("Extra space", space, row);
        setNote(noteText);
    }

    private void setNote(String text) {
        note.setText(text == null ? "" : text);
        note.setVisible(text != null && !text.isBlank());
        note.setManaged(note.isVisible());
    }

    private int add(String key, String val, int row) {
        Label k = new Label(key);
        k.getStyleClass().add("complexity-key");
        grid.addRow(row, k, value(val));
        return row + 1;
    }

    private static Label value(String v) {
        Label l = new Label(v);
        l.getStyleClass().add("complexity-value");
        l.setWrapText(true);
        return l;
    }

    private static Label head(String v) {
        Label l = new Label(v);
        l.getStyleClass().add("table-head");
        return l;
    }
}
