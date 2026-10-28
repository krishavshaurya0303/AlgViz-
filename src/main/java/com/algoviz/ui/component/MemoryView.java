package com.algoviz.ui.component;

import com.algoviz.engine.memory.MemoryLayout;
import com.algoviz.engine.memory.MemoryModel;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Shows dynamic memory for the current step: the operation's variables on the
 * stack and every node as a block on the heap, with addresses instead of arrows.
 * A toggle switches between the C model (malloc / free) and the Java model
 * (new / garbage collection); the owning controller rebuilds the layout when it changes.
 */
public final class MemoryView extends VBox {

    private final ObjectProperty<MemoryModel> model = new SimpleObjectProperty<>(MemoryModel.C);
    private final BooleanProperty expanded = new SimpleBooleanProperty(true);
    private final Label note = new Label();
    private final VBox stack = new VBox(3);
    private final HBox heap = new HBox(8);
    private final ScrollPane heapScroll = new ScrollPane(heap);

    public MemoryView() {
        getStyleClass().addAll("card", "memory-view");
        heap.setPadding(new Insets(6, 6, 10, 6)); // room for a hovered block to grow; scrollbar stays below

        Label title = new Label("Memory");
        title.getStyleClass().add("card-title");
        ToggleGroup group = new ToggleGroup();
        ToggleButton c = modelButton("C: malloc / free", MemoryModel.C, group,
                "Nodes come from malloc(sizeof(struct node)); removing a node calls free().");
        ToggleButton java = modelButton("Java: new / GC", MemoryModel.JAVA, group,
                "Nodes come from new Node(v); a removed node becomes garbage for the garbage collector.");
        group.selectedToggleProperty().addListener((o, a, v) -> {
            if (v == null) {
                a.setSelected(true); // keep one model selected
            } else {
                model.set((MemoryModel) v.getUserData());
            }
        });
        model.addListener((o, a, v) -> (v == MemoryModel.C ? c : java).setSelected(true));
        c.setSelected(true);
        HBox toggle = new HBox(c, java);
        toggle.getStyleClass().add("segmented");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        title.setTooltip(new Tooltip("Addresses are illustrative, not real machine addresses."));
        Button hide = new Button("Hide");
        hide.getStyleClass().add("ghost");
        hide.setTooltip(new Tooltip("Show or hide the memory view"));
        hide.setOnAction(e -> expanded.set(!expanded.get()));
        note.getStyleClass().add("memory-note");
        note.setWrapText(true);
        // The note sits in the header line, beside the toggle, so it costs no height of its own.
        // Two lines fit next to the toggle buttons; the panel never changes height between steps.
        note.setMinHeight(34);
        note.setMaxHeight(34);
        note.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(note, Priority.ALWAYS);
        HBox header = new HBox(14, title, note, spacer, toggle, hide);
        header.setAlignment(Pos.CENTER_LEFT);

        Label stackTitle = new Label("Stack");
        stackTitle.getStyleClass().add("memory-heading");
        VBox stackBox = new VBox(3, stackTitle, stack);
        stackBox.getStyleClass().add("memory-stack");
        stackBox.setMinWidth(150);

        Label heapTitle = new Label("Heap (illustrative addresses)");
        heapTitle.getStyleClass().add("memory-heading");
        heapScroll.setFitToHeight(true);
        heapScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox heapBox = new VBox(0, heapTitle, heapScroll);
        HBox.setHgrow(heapBox, Priority.ALWAYS);

        HBox body = new HBox(14, stackBox, heapBox);
        getChildren().addAll(header, body);
        expanded.addListener((o, a, v) -> {
            for (Node n : new Node[]{note, body, toggle}) {
                n.setVisible(v);
                n.setManaged(v);
            }
            hide.setText(v ? "Hide" : "Show memory");
        });
        // Never squeeze the blocks; the drawing above gives up space instead.
        setMinHeight(Region.USE_PREF_SIZE);
    }

    public ObjectProperty<MemoryModel> modelProperty() {
        return model;
    }

    public MemoryModel model() {
        return model.get();
    }

    /** Whether the stack and heap are shown; when false only the header line remains. */
    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void show(MemoryLayout layout) {
        stack.getChildren().clear();
        heap.getChildren().clear();
        if (layout == null) {
            note.setText("");
            return;
        }
        note.setText(layout.note());
        for (MemoryLayout.Variable v : layout.stack()) {
            Label name = new Label(v.name());
            name.getStyleClass().add("memory-name");
            Label value = new Label(v.value());
            value.getStyleClass().add("memory-value");
            if (v.changed()) {
                value.getStyleClass().add("changed");
            }
            Region gap = new Region();
            HBox.setHgrow(gap, Priority.ALWAYS);
            stack.getChildren().add(new HBox(8, name, gap, value));
        }
        Node focus = null;
        for (MemoryLayout.Block b : layout.heap()) {
            VBox box = block(b);
            heap.getChildren().add(box);
            if (b.state() == MemoryLayout.BlockState.NEW || b.state() == MemoryLayout.BlockState.FREED
                    || b.state() == MemoryLayout.BlockState.GARBAGE && focus == null) {
                focus = box;
            }
        }
        scrollTo(focus);
    }

    /** Scrolls the heap so the block that just changed is in view. */
    private void scrollTo(Node block) {
        if (block == null) {
            return;
        }
        Platform.runLater(() -> {
            heap.applyCss();
            heap.layout();
            double content = heap.getWidth();
            double view = heapScroll.getViewportBounds().getWidth();
            if (content > view) {
                double target = block.getBoundsInParent().getMinX() - (view - block.getBoundsInParent().getWidth()) / 2;
                heapScroll.setHvalue(Math.max(0, Math.min(1, target / (content - view))));
            }
        });
    }

    private static VBox block(MemoryLayout.Block b) {
        Label address = new Label(b.address());
        address.getStyleClass().add("memory-address");
        Label state = new Label(switch (b.state()) {
            case NEW -> "just allocated";
            case FREED -> "freed";
            case GARBAGE -> "unreachable";
            case LIVE -> b.type();
        });
        state.getStyleClass().add("memory-state");
        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(0);
        int row = 0;
        for (MemoryLayout.Field f : b.fields()) {
            Label name = new Label(f.name());
            name.getStyleClass().add("memory-name");
            Label value = new Label(f.value());
            value.getStyleClass().add("memory-value");
            if (f.changed()) {
                value.getStyleClass().add("changed");
            }
            fields.addRow(row++, name, value);
        }
        Region gap = new Region();
        HBox.setHgrow(gap, Priority.ALWAYS);
        HBox top = new HBox(8, address, gap, state); // address and type share one line
        top.setAlignment(Pos.BASELINE_LEFT);
        VBox box = new VBox(3, top, fields);
        box.getStyleClass().addAll("memory-block", "state-" + b.state().name().toLowerCase());
        return box;
    }

    private ToggleButton modelButton(String text, MemoryModel m, ToggleGroup group, String tip) {
        ToggleButton b = new ToggleButton(text);
        b.setUserData(m);
        b.setToggleGroup(group);
        b.setTooltip(new Tooltip(tip));
        return b;
    }
}
