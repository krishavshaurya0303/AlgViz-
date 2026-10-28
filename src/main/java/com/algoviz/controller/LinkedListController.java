package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.linkedlist.ListOperation;
import com.algoviz.engine.linkedlist.ListSnapshot;
import com.algoviz.engine.linkedlist.ListStep;
import com.algoviz.engine.linkedlist.SinglyLinkedList;
import com.algoviz.engine.memory.MemoryLayout;
import com.algoviz.engine.memory.MemoryModel;
import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.component.EmptyState;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import com.algoviz.ui.render.LinkedListCanvas;
import com.algoviz.ui.theme.Palette;
import com.algoviz.util.InvalidInputException;
import com.algoviz.util.ValidationUtils;
import com.algoviz.service.CSourceCatalog;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Linked List Lab: list operations with explicit pointer visualization. */
public final class LinkedListController extends VisualizerController<ListStep> {

    @FXML private TextField valueField;
    @FXML private TextField indexField;
    @FXML private Button insertHeadButton;
    @FXML private Button insertTailButton;
    @FXML private Button insertAtButton;
    @FXML private Button deleteHeadButton;
    @FXML private Button deleteTailButton;
    @FXML private Button deleteAtButton;
    @FXML private Button searchButton;
    @FXML private Button traverseButton;
    @FXML private Button reverseButton;
    @FXML private Button sampleButton;
    @FXML private Button clearButton;
    @FXML private Label inputMessage;
    @FXML private LinkedListCanvas canvas;
    @FXML private VBox emptyHolder;
    @FXML private Label codeTitle;

    private final SinglyLinkedList list = new SinglyLinkedList();
    private final Random random = new Random();
    private final EmptyState emptyState = new EmptyState(IconType.LINKED_LIST, "The list is empty",
            "head points to null. Insert a value, or create a sample list to try traversal and reversal.");
    private ListOperation shownCode;

    public LinkedListController(AppContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        // An empty message row takes no space, so the board sits right under the toolbar.
        inputMessage.managedProperty().bind(inputMessage.textProperty().isNotEmpty());
        tip(insertHeadButton, "Insert the value at the front — O(1)");
        tip(insertTailButton, "Walk to the last node and append — O(n)");
        tip(insertAtButton, "Insert the value so that it ends up at the given index — O(n)");
        tip(deleteHeadButton, "Remove the first node — O(1)");
        tip(deleteTailButton, "Walk to the second-to-last node and unlink the last — O(n)");
        tip(deleteAtButton, "Remove the node at the given index — O(n)");
        tip(searchButton, "Find the first node holding the value — O(n)");
        tip(traverseButton, "Visit every node from head to tail — O(n)");
        tip(reverseButton, "Reverse the list in place by redirecting every next pointer — O(n)");
        reverseButton.setGraphic(Icons.create(IconType.REVERSE, 14));
        searchButton.setGraphic(Icons.create(IconType.SEARCH, 14));
        sampleButton.setGraphic(Icons.create(IconType.SHUFFLE, 14));
        clearButton.setGraphic(Icons.create(IconType.TRASH, 14));
        valueField.setOnAction(e -> insertTail());
        valueField.textProperty().addListener((o, a, v) -> valueField.getStyleClass().remove("invalid"));
        indexField.textProperty().addListener((o, a, v) -> indexField.getStyleClass().remove("invalid"));

        emptyState.setAction("Create sample list", this::sample);
        emptyHolder.getChildren().add(emptyState);

        legend.item(Palette.CYAN, "current")
                .item(Palette.BLUE, "Visited")
                .item(Palette.GREEN, "New / found")
                .item(Palette.CORAL, "Removing")
                .item(Palette.AMBER, "Pointer just changed")
                .item(Palette.BG_1, Palette.TEXT_3, "next box (slash = null)");


        initPlayback();
        load(list.currentState(ListOperation.TRAVERSE, "The list is empty: head points to null."), false);
    }

    private static void tip(Button b, String text) {
        b.setTooltip(new Tooltip(text));
    }

    // ------------------------------------------------------------ inputs

    private Integer readValue() {
        try {
            int v = ValidationUtils.parseInt(valueField.getText(), "value", AppConfig.LIST_MIN_VALUE,
                    AppConfig.LIST_MAX_VALUE);
            return v;
        } catch (InvalidInputException e) {
            valueField.getStyleClass().add("invalid");
            message(e.getMessage(), true);
            valueField.requestFocus();
            return null;
        }
    }

    private Integer readIndex(int max) {
        try {
            return ValidationUtils.parseInt(indexField.getText(), "index", 0, max);
        } catch (InvalidInputException e) {
            indexField.getStyleClass().add("invalid");
            message(e.getMessage() + (list.isEmpty() ? "" : " The list currently has " + list.size() + " nodes."), true);
            indexField.requestFocus();
            return null;
        }
    }

    private boolean hasRoom() {
        if (list.size() >= AppConfig.LIST_MAX_NODES) {
            message("The lab holds at most " + AppConfig.LIST_MAX_NODES + " nodes so every pointer stays visible. "
                    + "Delete a node first.", true);
            return false;
        }
        return true;
    }

    @FXML
    private void insertHead() {
        Integer v = readValue();
        if (v != null && hasRoom()) {
            run(list.insertAtHead(v));
        }
    }

    @FXML
    private void insertTail() {
        Integer v = readValue();
        if (v != null && hasRoom()) {
            run(list.insertAtTail(v));
        }
    }

    @FXML
    private void insertAt() {
        Integer v = readValue();
        if (v == null || !hasRoom()) {
            return;
        }
        Integer i = readIndex(list.size());
        if (i != null) {
            run(list.insertAt(i, v));
        }
    }

    @FXML
    private void deleteHead() {
        run(list.deleteHead());
    }

    @FXML
    private void deleteTail() {
        run(list.deleteTail());
    }

    @FXML
    private void deleteAt() {
        if (list.isEmpty()) {
            run(list.deleteAt(0));
            return;
        }
        Integer i = readIndex(list.size() - 1);
        if (i != null) {
            run(list.deleteAt(i));
        }
    }

    @FXML
    private void search() {
        Integer v = readValue();
        if (v != null) {
            run(list.search(v));
        }
    }

    @FXML
    private void traverse() {
        run(list.traverse());
    }

    @FXML
    private void reverse() {
        run(list.reverse());
    }

    @FXML
    private void sample() {
        list.clear();
        int n = 5;
        for (int k = 0; k < n; k++) {
            list.appendSilently(10 + random.nextInt(90));
        }
        message("Created a sample list with " + n + " nodes.", false);
        load(list.currentState(ListOperation.TRAVERSE, "Sample list " + list.values()
                + ". Try Reverse to watch every pointer turn around."), false);
    }

    @FXML
    private void clearList() {
        if (list.isEmpty()) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "All " + list.size() + " nodes will become unreachable.", ButtonType.CANCEL, ButtonType.OK);
        confirm.setTitle("Clear list");
        confirm.setHeaderText("Clear the whole list?");
        confirm.getDialogPane().getStylesheets().addAll(canvas.getScene().getStylesheets());
        confirm.initOwner(canvas.getScene().getWindow());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            run(list.clear());
        }
    }

    private void run(ExecutionTrace<ListStep> trace) {
        ListStep last = trace.last();
        if (last.status() == StepStatus.FAILURE) {
            message(last.explanation(), true);
        } else {
            message("", false);
        }
        load(trace, true);
    }

    /** {@code stepThrough}: start at step 0 so the user walks the operation with Next; otherwise show the end state. */
    private void load(ExecutionTrace<ListStep> trace, boolean stepThrough) {
        if (stepThrough && trace.size() > 1) {
            driver.load(trace);
        } else {
            driver.loadAtEnd(trace);
        }
    }

    private void message(String text, boolean error) {
        inputMessage.setText(text);
        inputMessage.getStyleClass().removeAll("error", "success");
        if (!text.isEmpty()) {
            inputMessage.getStyleClass().add(error ? "error" : "success");
        }
    }

    @Override
    protected MemoryLayout memoryLayout(ExecutionTrace<ListStep> trace, int index, MemoryModel model) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < index; i++) {
            trace.get(i).snapshot().nodes().forEach(n -> seen.put(n.id(), n.value()));
        }
        ListSnapshot prev = index > 0 ? trace.get(index - 1).snapshot() : null;
        return MemoryLayout.ofList(trace.get(index).snapshot(), prev, seen, model);
    }

    // ------------------------------------------------------------ rendering

    @Override
    protected void render(ListStep previous, ListStep current, double millis) {
        if (current == null) {
            return;
        }
        canvas.show(previous, current, millis);
        emptyHolder.setVisible(current.snapshot().nodes().isEmpty());
        ListOperation op = current.operation();
        if (op != shownCode) {
            shownCode = op;
            pseudocodePanel.setCode(op.pseudocode(), CSourceCatalog.find("list/" + op.name()).orElse(null));
            codeTitle.setText("Code for " + op.label().toLowerCase());
        }
    }

    // ------------------------------------------------------------ module

    @Override
    public void onShow(NavigationRequest request) {
    }

    @Override
    public List<Node> headerActions() {
        Button learn = new Button("Learn about linked lists");
        learn.setGraphic(Icons.create(IconType.LIBRARY, 15));
        learn.getStyleClass().add("ghost");
        learn.setOnAction(e -> context.navigate(ModuleId.LIBRARY, NavigationRequest.select("singly-linked-list")));
        return List.of(learn);
    }

    @Override
    public String helpText() {
        return "Enter a value (and an index where needed) and choose an operation. Indices start at 0. "
                + "Each box is a node: the left part holds the value, the right part is the next pointer; a slash "
                + "means null. Helper pointers such as current, prev and next are shown under the nodes. Amber "
                + "arrows are pointers that changed in this step. Nodes that are not linked into the list yet "
                + "(or are being removed) float above the row.";
    }
}
