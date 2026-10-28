package com.algoviz.ui.component;

import com.algoviz.model.CodeListing;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Numbered code listing with the current line highlighted. It shows either the
 * pseudocode or, when one exists, the equivalent C code; steps always name a
 * pseudocode line, and the C listing maps that line onto its own lines.
 */
public final class PseudocodePanel extends VBox {

    private final BooleanProperty showC = new SimpleBooleanProperty(false);
    private final List<HBox> rows = new ArrayList<>();
    /** The lines themselves; C lines never wrap, so wide ones scroll sideways like in an editor. */
    private final VBox listing = new VBox();
    private final ScrollPane scroller = new ScrollPane(listing);
    private List<String> pseudocode = List.of();
    private CodeListing cCode;
    private int pseudoLine = -1;
    private HBox toggle;

    public PseudocodePanel() {
        getStyleClass().add("pseudocode");
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.prefViewportHeightProperty().bind(listing.heightProperty());
        // Short listings still stretch to the full box, so the highlight bar spans the whole width.
        listing.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> scroller.getViewportBounds().getWidth(), scroller.viewportBoundsProperty()));
        getChildren().add(scroller);
        showC.addListener((o, a, v) -> rebuild());
        setCode(List.of());
    }

    public void setCode(List<String> code) {
        setCode(code, null);
    }

    /** Pseudocode plus an optional C listing of the same algorithm. */
    public void setCode(List<String> code, CodeListing c) {
        pseudocode = code;
        cCode = c;
        pseudoLine = -1;
        if (toggle != null) {
            toggle.setDisable(c == null);
        }
        rebuild();
    }

    /** True when the C listing is on screen (the C view is chosen and this algorithm has one). */
    public boolean showingC() {
        return showC.get() && cCode != null;
    }

    public BooleanProperty showCProperty() {
        return showC;
    }

    /** "Pseudocode | C" switch for the card header; one instance per panel. */
    public Node languageToggle() {
        if (toggle == null) {
            ToggleGroup group = new ToggleGroup();
            ToggleButton pseudo = new ToggleButton("Pseudocode");
            ToggleButton c = new ToggleButton("C");
            pseudo.setToggleGroup(group);
            c.setToggleGroup(group);
            c.setTooltip(new Tooltip("The same algorithm written in C; the highlighted lines follow each step."));
            group.selectedToggleProperty().addListener((o, a, v) -> {
                if (v == null) {
                    a.setSelected(true); // one language is always chosen
                } else {
                    showC.set(v == c);
                }
            });
            showC.addListener((o, a, v) -> (v ? c : pseudo).setSelected(true));
            (showC.get() ? c : pseudo).setSelected(true);
            toggle = new HBox(pseudo, c);
            toggle.getStyleClass().addAll("segmented", "code-toggle");
            toggle.setDisable(cCode == null);
        }
        return toggle;
    }

    private void rebuild() {
        listing.getChildren().clear();
        rows.clear();
        boolean c = showingC();
        scroller.setFitToWidth(!c);
        scroller.setHvalue(0);
        List<String> lines = c ? cCode.lines() : pseudocode;
        if (lines.isEmpty()) {
            Label none = new Label("Run an operation to see its pseudocode.");
            none.getStyleClass().add("pseudocode-empty");
            listing.getChildren().add(none);
            return;
        }
        for (int i = 0; i < lines.size(); i++) {
            Label no = new Label(String.valueOf(i + 1));
            no.getStyleClass().add("line-no");
            Label text = new Label(lines.get(i));
            text.getStyleClass().add("code-text");
            text.setWrapText(!c);
            text.setMinHeight(Region.USE_PREF_SIZE);
            if (c) {
                text.setMinWidth(Region.USE_PREF_SIZE);
            }
            HBox row = new HBox(no, text);
            row.getStyleClass().add("code-line");
            rows.add(row);
            listing.getChildren().add(row);
        }
        applyHighlight();
    }

    /** Highlights a zero-based pseudocode line (and its C lines in the C view); -1 clears it. */
    public void highlight(int line) {
        pseudoLine = line;
        applyHighlight();
    }

    private void applyHighlight() {
        for (HBox row : rows) {
            row.getStyleClass().remove("current");
        }
        if (pseudoLine < 0) {
            return;
        }
        List<Integer> targets = showingC() ? cCode.linesFor(pseudoLine) : List.of(pseudoLine);
        for (int i : targets) {
            if (i < rows.size()) {
                rows.get(i).getStyleClass().add("current");
            }
        }
    }
}
