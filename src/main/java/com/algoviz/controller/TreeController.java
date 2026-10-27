package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.tree.BinarySearchTree;
import com.algoviz.engine.tree.TreeOperation;
import com.algoviz.engine.tree.TreeStep;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.MetricType;
import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.component.EmptyState;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import com.algoviz.ui.render.TreeCanvas;
import com.algoviz.ui.theme.Palette;
import com.algoviz.util.InvalidInputException;
import com.algoviz.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Tree Visualizer: BST operations, traversal playback and tree rendering. */
public final class TreeController extends VisualizerController<TreeStep> {

    @FXML private TextField valueField;
    @FXML private Button insertButton;
    @FXML private Button searchButton;
    @FXML private Button deleteButton;
    @FXML private ComboBox<TreeOperation> traversalBox;
    @FXML private Button traverseButton;
    @FXML private Button sampleButton;
    @FXML private Button clearButton;
    @FXML private Label inputMessage;
    @FXML private TreeCanvas canvas;
    @FXML private HBox outputStrip;
    @FXML private Label outputTitle;
    @FXML private Label outputValues;
    @FXML private VBox emptyHolder;
    @FXML private Label codeTitle;
    @FXML private Label aboutLabel;

    private final BinarySearchTree tree = new BinarySearchTree();
    private final Random random = new Random();
    private final EmptyState emptyState = new EmptyState(IconType.TREE, "The tree is empty",
            "Insert a value above, or generate a sample tree to explore searching, deletion and traversals.");
    private TreeOperation shownCode;

    public TreeController(AppContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        traversalBox.getItems().setAll(TreeOperation.INORDER, TreeOperation.PREORDER,
                TreeOperation.POSTORDER, TreeOperation.LEVEL_ORDER);
        traversalBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(TreeOperation op) {
                return op == null ? "" : op.label();
            }

            @Override
            public TreeOperation fromString(String s) {
                return null;
            }
        });
        traversalBox.getSelectionModel().selectFirst();
        valueField.setOnAction(e -> insert());
        valueField.textProperty().addListener((o, a, v) -> valueField.getStyleClass().remove("invalid"));
        valueField.setTooltip(new Tooltip("Whole number from " + AppConfig.TREE_MIN_VALUE + " to "
                + AppConfig.TREE_MAX_VALUE + ". Press Enter to insert."));
        insertButton.setGraphic(Icons.create(IconType.PLUS, 14));
        searchButton.setGraphic(Icons.create(IconType.SEARCH, 14));
        deleteButton.setGraphic(Icons.create(IconType.MINUS, 14));
        sampleButton.setGraphic(Icons.create(IconType.SHUFFLE, 14));
        clearButton.setGraphic(Icons.create(IconType.TRASH, 14));

        emptyState.setAction("Generate sample tree", this::sample);
        emptyHolder.getChildren().add(emptyState);

        legend.item(Palette.CYAN, "Examining")
                .item(Palette.INDIGO, "Path / stack")
                .item(Palette.GREEN, "Found / inserted / visited")
                .item(Palette.AMBER, "Successor")
                .item(Palette.CORAL, "Deleting");

        metricsPanel.configure(List.of(MetricType.COMPARISONS, MetricType.NODES_VISITED,
                        MetricType.NODE_COUNT, MetricType.TREE_HEIGHT),
                List.of(new String[]{STEP, "Position in the recorded steps of the current operation."},
                        new String[]{ELAPSED, "Wall-clock time spent in Play mode (paused time excluded)."}));

        AlgorithmMetadata bst = context.catalog().binarySearchTree();
        complexityCard.showOperations(bst.operations(), "Average", "Worst", bst.complexityNote());
        aboutLabel.setText("This is a standard binary search tree: it does not rebalance itself, so its shape "
                + "depends on insertion order. Duplicates are rejected. Nodes are laid out left-to-right in sorted "
                + "(inorder) order, one row per depth level.");

        initPlayback();
        load(tree.currentState("The tree is empty. Insert a value or generate a sample tree."), false);
    }

    // ------------------------------------------------------------ operations

    private Integer readValue() {
        try {
            int v = ValidationUtils.parseInt(valueField.getText(), "value", AppConfig.TREE_MIN_VALUE,
                    AppConfig.TREE_MAX_VALUE);
            message("", false);
            return v;
        } catch (InvalidInputException e) {
            valueField.getStyleClass().add("invalid");
            message(e.getMessage(), true);
            valueField.requestFocus();
            return null;
        }
    }

    @FXML
    private void insert() {
        Integer v = readValue();
        if (v == null) {
            return;
        }
        if (tree.size() >= AppConfig.TREE_MAX_NODES && !tree.contains(v)) {
            message("The visualizer holds at most " + AppConfig.TREE_MAX_NODES + " nodes. Delete some or clear the tree.",
                    true);
            return;
        }
        load(tree.insert(v), true);
        valueField.selectAll();
    }

    @FXML
    private void search() {
        Integer v = readValue();
        if (v != null) {
            load(tree.search(v), true);
        }
    }

    @FXML
    private void delete() {
        Integer v = readValue();
        if (v != null) {
            load(tree.delete(v), true);
        }
    }

    @FXML
    private void traverse() {
        load(tree.traverse(traversalBox.getValue()), true);
    }

    @FXML
    private void sample() {
        tree.clear();
        TreeSet<Integer> values = new TreeSet<>();
        while (values.size() < 11) {
            values.add(5 + random.nextInt(95));
        }
        // Insert medians first so the sample is reasonably balanced, then shuffle a few leaves.
        List<Integer> sorted = new ArrayList<>(values);
        List<Integer> order = new ArrayList<>();
        medianOrder(sorted, 0, sorted.size() - 1, order);
        List<Integer> tail = new ArrayList<>(order.subList(7, order.size()));
        Collections.shuffle(tail, random);
        for (int i = 0; i < order.size(); i++) {
            tree.insertSilently(i < 7 ? order.get(i) : tail.get(i - 7));
        }
        message("Generated a sample tree with " + tree.size() + " nodes.", false);
        load(tree.currentState("Sample tree with " + tree.size() + " nodes and height " + tree.height()
                + ". Try searching for a value or deleting a node with two children."), false);
    }

    private static void medianOrder(List<Integer> sorted, int lo, int hi, List<Integer> out) {
        if (lo > hi) {
            return;
        }
        int mid = (lo + hi) >>> 1;
        out.add(sorted.get(mid));
        // breadth-like: left then right halves recursively
        medianOrder(sorted, lo, mid - 1, out);
        medianOrder(sorted, mid + 1, hi, out);
    }

    @FXML
    private void clearTree() {
        if (tree.isEmpty()) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "All " + tree.size() + " nodes will be removed.", ButtonType.CANCEL, ButtonType.OK);
        confirm.setTitle("Clear tree");
        confirm.setHeaderText("Clear the whole tree?");
        confirm.getDialogPane().getStylesheets().addAll(canvas.getScene().getStylesheets());
        confirm.initOwner(canvas.getScene().getWindow());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            tree.clear();
            message("The tree was cleared.", false);
            load(tree.currentState("The tree is empty. Insert a value or generate a sample tree."), false);
        }
    }

    private void load(ExecutionTrace<TreeStep> trace, boolean play) {
        if (play && trace.size() > 1) {
            driver.loadAndPlay(trace);
        } else {
            driver.loadAtEnd(trace);
        }
        if (trace.last().status() == StepStatus.FAILURE && play) {
            message(trace.last().explanation(), true);
        }
    }

    private void message(String text, boolean error) {
        inputMessage.setText(text);
        inputMessage.getStyleClass().removeAll("error", "success");
        if (!text.isEmpty()) {
            inputMessage.getStyleClass().add(error ? "error" : "success");
        }
    }

    // ------------------------------------------------------------ rendering

    @Override
    protected void render(TreeStep previous, TreeStep current, double millis) {
        if (current == null) {
            canvas.clear();
            return;
        }
        canvas.show(previous != null && previous.tree() != null ? previous : null, current, millis);
        emptyHolder.setVisible(current.tree().isEmpty());
        TreeOperation op = current.operation();
        if (op != shownCode && op != TreeOperation.VIEW) {
            shownCode = op;
            pseudocodePanel.setCode(op.pseudocode());
            codeTitle.setText("PSEUDOCODE · " + op.label().toUpperCase());
        }
        if (op.isTraversal() && !current.output().isEmpty()) {
            outputTitle.setText(op.label() + " output");
            outputValues.setText(current.output().stream().map(String::valueOf).collect(Collectors.joining("  →  ")));
            outputStrip.setVisible(true);
        } else {
            outputStrip.setVisible(false);
        }
    }

    // ------------------------------------------------------------ module

    @Override
    public void onShow(NavigationRequest request) {
        if ("demo".equals(request.action())) {
            sample();
        }
    }

    @Override
    public List<Node> headerActions() {
        Button learn = new Button("Learn about BSTs");
        learn.setGraphic(Icons.create(IconType.LIBRARY, 15));
        learn.getStyleClass().add("ghost");
        learn.setOnAction(e -> context.navigate(ModuleId.LIBRARY, NavigationRequest.select("bst")));
        return List.of(learn);
    }

    @Override
    public String helpText() {
        return "Type a value and press Insert (or Enter), Search or Delete. Each operation is recorded step by "
                + "step: the cyan node is being compared, indigo nodes show the path taken, green marks the "
                + "result. Deleting a node with two children highlights its inorder successor in amber. "
                + "Traversals output the visited values at the bottom. Step backward to see the tree as it was "
                + "before the operation.";
    }
}
