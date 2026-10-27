package com.algoviz.controller;

import com.algoviz.model.AlgorithmCategory;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.ComplexityEntry;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import com.algoviz.ui.component.PseudocodePanel;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Algorithm Library: structured documentation for every algorithm, independent of the animation. */
public final class LibraryController extends StaticPageController {

    @FXML private VBox entryList;
    @FXML private VBox detail;
    @FXML private ScrollPane detailScroll;

    private final ToggleGroup group = new ToggleGroup();
    private final Map<String, ToggleButton> buttons = new HashMap<>();

    public LibraryController(AppContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        List<AlgorithmMetadata> all = context.catalog().all();
        status.set(all.size() + " entries");
        AlgorithmCategory lastCategory = null;
        for (AlgorithmMetadata m : all) {
            if (m.category() != lastCategory) {
                lastCategory = m.category();
                Label g = new Label(m.category().displayName().toUpperCase());
                g.getStyleClass().add("library-group");
                entryList.getChildren().add(g);
            }
            ToggleButton b = new ToggleButton(m.name());
            b.getStyleClass().setAll("library-item");
            b.setToggleGroup(group);
            b.setOnAction(e -> {
                b.setSelected(true);
                show(m);
            });
            buttons.put(m.id(), b);
            entryList.getChildren().add(b);
        }
        select(all.get(0).id());
    }

    private void select(String id) {
        ToggleButton b = buttons.get(id);
        if (b != null) {
            b.setSelected(true);
            context.catalog().byId(id).ifPresent(this::show);
        }
    }

    @Override
    public void onShow(NavigationRequest request) {
        if (request.algorithmId() != null) {
            select(request.algorithmId());
        }
    }

    private void show(AlgorithmMetadata m) {
        detail.getChildren().clear();
        detailScroll.setVvalue(0);

        Label category = new Label(m.category().displayName());
        category.getStyleClass().addAll("chip", "accent");
        FlowPane chips = new FlowPane(6, 6, category);
        if (m.stable() != null) {
            Label s = new Label(m.stable() ? "Stable" : "Not stable");
            s.getStyleClass().addAll("chip", m.stable() ? "success" : "warning");
            chips.getChildren().add(s);
        }
        if (m.inPlace() != null) {
            Label s = new Label(m.inPlace() ? "In place" : "Uses extra memory");
            s.getStyleClass().addAll("chip", m.inPlace() ? "success" : "warning");
            chips.getChildren().add(s);
        }

        Label title = new Label(m.name());
        title.getStyleClass().add("doc-title");
        Label summary = new Label(m.summary());
        summary.getStyleClass().add("doc-summary");
        summary.setWrapText(true);

        Button open = new Button("Open in " + m.category().module().title());
        open.getStyleClass().add("primary");
        open.setGraphic(Icons.create(IconType.PLAY, 13));
        open.setOnAction(e -> context.navigate(m.category().module(), NavigationRequest.select(m.id())));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox head = new HBox(12, new VBox(8, chips, title), spacer, open);
        head.setAlignment(Pos.BOTTOM_LEFT);

        detail.getChildren().addAll(head, summary);
        detail.getChildren().add(section("WHEN IS IT USEFUL?"));
        detail.getChildren().add(text(m.whenUseful()));

        detail.getChildren().add(section("HOW IT WORKS"));
        VBox steps = new VBox(8);
        for (int i = 0; i < m.howItWorks().size(); i++) {
            Label no = new Label(String.valueOf(i + 1));
            no.getStyleClass().add("doc-step-no");
            Label t = text(m.howItWorks().get(i));
            HBox.setHgrow(t, Priority.ALWAYS);
            HBox row = new HBox(10, no, t);
            row.setAlignment(Pos.TOP_LEFT);
            steps.getChildren().add(row);
        }
        detail.getChildren().add(steps);

        detail.getChildren().add(section("PSEUDOCODE"));
        PseudocodePanel code = new PseudocodePanel();
        code.setCode(m.pseudocode());
        detail.getChildren().add(code);

        detail.getChildren().add(section("COMPLEXITY (THEORETICAL)"));
        detail.getChildren().add(m.operations().isEmpty() ? algorithmTable(m) : operationTable(m.operations()));
        if (m.complexityNote() != null && !m.complexityNote().isBlank()) {
            Label note = new Label(m.complexityNote());
            note.getStyleClass().add("note-text");
            note.setWrapText(true);
            detail.getChildren().add(note);
        }
    }

    private static Label section(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("doc-heading");
        return l;
    }

    private static Label text(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("doc-text");
        l.setWrapText(true);
        l.setMinHeight(Region.USE_PREF_SIZE);
        return l;
    }

    private static GridPane table(String... headers) {
        GridPane g = new GridPane();
        g.getStyleClass().add("card");
        g.setHgap(28);
        g.setVgap(9);
        for (int i = 0; i < headers.length; i++) {
            Label h = new Label(headers[i]);
            h.getStyleClass().add("table-head");
            g.add(h, i, 0);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(i == headers.length - 1 ? Priority.ALWAYS : Priority.NEVER);
            g.getColumnConstraints().add(cc);
        }
        return g;
    }

    private static GridPane algorithmTable(AlgorithmMetadata m) {
        GridPane g = table("Best", "Average", "Worst", "Space");
        g.add(mono(m.bestTime()), 0, 1);
        g.add(mono(m.averageTime()), 1, 1);
        g.add(mono(m.worstTime()), 2, 1);
        g.add(mono(m.space()), 3, 1);
        return g;
    }

    private static GridPane operationTable(List<ComplexityEntry> ops) {
        GridPane g = table("Operation", "Average", "Worst", "Notes");
        int row = 1;
        for (ComplexityEntry e : ops) {
            Label op = new Label(e.operation());
            op.getStyleClass().add("table-cell-text");
            Label note = new Label(e.note());
            note.getStyleClass().add("note-text");
            g.addRow(row++, op, mono(e.average()), mono(e.worst()), note);
        }
        return g;
    }

    private static Label mono(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("table-cell-mono");
        return l;
    }

    @Override
    public String helpText() {
        return "Select an entry on the left to read how it works, its pseudocode and its theoretical complexity. "
                + "\"Open in …\" jumps to the matching visualizer with the algorithm selected.";
    }
}
