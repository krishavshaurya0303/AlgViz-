package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.graph.GraphSearchAlgorithm;
import com.algoviz.engine.graph.GraphStep;
import com.algoviz.engine.graph.GridGraph;
import com.algoviz.engine.graph.SearchInput;
import com.algoviz.engine.graph.SearchOutcome;
import com.algoviz.model.MetricType;
import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import com.algoviz.ui.render.GridCanvas;
import com.algoviz.ui.theme.Palette;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.service.CSourceCatalog;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.Random;

/** Graph Explorer: grid editing, trace generation and grid rendering. */
public final class GraphController extends VisualizerController<GraphStep> {

    private enum Tool { WALL, WEIGHT, ERASE, START, TARGET }

    @FXML private FlowPane toolbar;
    @FXML private ComboBox<GraphSearchAlgorithm> algorithmBox;
    @FXML private ToggleButton wallTool;
    @FXML private ToggleButton weightTool;
    @FXML private ToggleButton eraseTool;
    @FXML private ToggleButton startTool;
    @FXML private ToggleButton targetTool;
    @FXML private Button mazeButton;
    @FXML private Button weightsButton;
    @FXML private Button clearButton;
    @FXML private Label inputMessage;
    @FXML private GridCanvas canvas;
    @FXML private Label banner;
    @FXML private Label rulesLabel;

    private final GridGraph grid = new GridGraph(AppConfig.GRID_ROWS, AppConfig.GRID_COLS, AppConfig.WEIGHTED_CELL_COST);
    private final ToggleGroup tools = new ToggleGroup();
    private final Random random = new Random();
    /** For wall/weight dragging: true = painting, false = erasing (decided on press). */
    private boolean paintValue;

    public GraphController(AppContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        // An empty message row takes no space, so the board sits right under the toolbar.
        inputMessage.managedProperty().bind(inputMessage.textProperty().isNotEmpty());
        algorithmBox.getItems().setAll(context.catalog().graphAlgorithms());
        algorithmBox.setCellFactory(l -> nameCell());
        algorithmBox.setButtonCell(nameCell());
        algorithmBox.getSelectionModel().selectFirst();
        algorithmBox.setOnAction(e -> {
            updateAlgorithmInfo();
            rebuild();
            context.recordActivity(ModuleId.GRAPH, selected().metadata().id());
        });

        for (ToggleButton b : List.of(wallTool, weightTool, eraseTool, startTool, targetTool)) {
            b.setToggleGroup(tools);
        }
        wallTool.setGraphic(Icons.create(IconType.WALL, 14));
        weightTool.setGraphic(Icons.create(IconType.WEIGHT, 14));
        eraseTool.setGraphic(Icons.create(IconType.ERASER, 14));
        startTool.setGraphic(Icons.create(IconType.PIN, 14));
        targetTool.setGraphic(Icons.create(IconType.FLAG, 14));
        weightTool.setTooltip(new Tooltip("Weighted cells cost " + AppConfig.WEIGHTED_CELL_COST
                + " to enter instead of 1. Only Dijkstra takes weights into account."));
        tools.selectToggle(wallTool);
        tools.selectedToggleProperty().addListener((o, old, now) -> {
            if (now == null) {
                tools.selectToggle(old); // always keep one tool selected
            }
        });
        mazeButton.setGraphic(Icons.create(IconType.SHUFFLE, 14));
        clearButton.setGraphic(Icons.create(IconType.TRASH, 14));

        legend.item(Palette.CELL_EMPTY, Palette.BORDER_STRONG, "Empty")
                .item(Palette.CELL_WALL, "Wall")
                .item(Palette.CELL_WEIGHT, Palette.AMBER, "Weight ×" + AppConfig.WEIGHTED_CELL_COST)
                .item(Palette.CELL_FRONTIER, "Frontier")
                .item(Palette.CELL_VISITED, "Visited")
                .item(Palette.AMBER, "Current")
                .item(Palette.CELL_PATH, "Path")
                .item(Palette.INDIGO, "Start")
                .item(Palette.CORAL, "Target");


        rulesLabel.setText("Movement is 4-directional: up, right, down, left — no diagonals, so walls can never "
                + "be cut through at corners. Entering a normal cell costs 1 and a weighted cell costs "
                + AppConfig.WEIGHTED_CELL_COST + ". BFS and DFS ignore weights. Without weighted cells every move "
                + "costs the same, so Dijkstra and BFS find equally short paths; Dijkstra only gains an advantage "
                + "when costs differ.");

        canvas.setGrid(grid);
        canvas.setShowDistances(context.settings().showGridDistances());
        canvas.setCellListener(this::onCell);
        grid.randomize(new Random(2024), 0.22, 0);

        initPlayback();
        updateAlgorithmInfo();
        rebuild();
    }

    private ListCell<GraphSearchAlgorithm> nameCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(GraphSearchAlgorithm item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        };
    }

    private GraphSearchAlgorithm selected() {
        return algorithmBox.getValue();
    }

    private void updateAlgorithmInfo() {
        AlgorithmMetadata m = selected().metadata();
        pseudocodePanel.setCode(m.pseudocode(), CSourceCatalog.find("graph/" + m.id()).orElse(null));
        complexityCard.show(selected().metadata());
    }

    private Tool tool() {
        Object t = tools.getSelectedToggle();
        if (t == weightTool) {
            return Tool.WEIGHT;
        }
        if (t == eraseTool) {
            return Tool.ERASE;
        }
        if (t == startTool) {
            return Tool.START;
        }
        if (t == targetTool) {
            return Tool.TARGET;
        }
        return Tool.WALL;
    }

    // ------------------------------------------------------------ editing

    private void onCell(int row, int col, MouseButton button, boolean dragging) {
        int node = grid.index(row, col);
        Tool t = button == MouseButton.SECONDARY ? Tool.ERASE : tool();
        boolean changed;
        switch (t) {
            case WALL -> {
                if (!dragging) {
                    paintValue = !grid.isWall(node);
                }
                changed = grid.isWall(node) != paintValue && grid.setWall(node, paintValue);
            }
            case WEIGHT -> {
                if (!dragging) {
                    paintValue = !grid.isWeighted(node);
                }
                changed = grid.isWeighted(node) != paintValue && grid.setWeighted(node, paintValue);
            }
            case ERASE -> {
                changed = grid.isWall(node) || grid.isWeighted(node);
                grid.setWall(node, false);
                grid.setWeighted(node, false);
            }
            case START -> {
                changed = node != grid.start() && grid.setStart(node);
                if (!changed && node == grid.target()) {
                    showMessage("The start cannot be placed on the target.", true);
                }
            }
            default -> {
                changed = node != grid.target() && grid.setTarget(node);
                if (!changed && node == grid.start()) {
                    showMessage("The target cannot be placed on the start.", true);
                }
            }
        }
        if (changed) {
            showMessage("", false);
            rebuild();
        }
    }

    @FXML
    private void randomWalls() {
        grid.randomize(random, 0.26, 0);
        rebuild();
    }

    @FXML
    private void randomWeighted() {
        grid.randomize(random, 0.18, 0.22);
        rebuild();
    }

    @FXML
    private void clearBoard() {
        grid.clearObstacles();
        rebuild();
    }

    private void showMessage(String text, boolean error) {
        inputMessage.setText(text);
        inputMessage.getStyleClass().removeAll("error");
        if (error) {
            inputMessage.getStyleClass().add("error");
        }
    }

    /** Re-runs the selected search on a copy of the grid; the result replaces the current run. */
    private void rebuild() {
        GraphSearchAlgorithm algorithm = selected();
        GridGraph copy = grid.copy();
        driver.suspend();
        canvas.redraw();
        runner.submit(() -> algorithm.run(new SearchInput(copy, copy.start(), copy.target())),
                this::onTraceReady,
                error -> explanationCard.show("Could not run " + algorithm.name() + ": " + error.getMessage(),
                        StepStatus.FAILURE));
    }

    private void onTraceReady(ExecutionTrace<GraphStep> trace) {
        driver.load(trace);
    }

    // ---------------------------------------------------------- rendering

    @Override
    protected void render(GraphStep previous, GraphStep current, double millis) {
        canvas.show(previous, current, millis);
        if (current == null || current.outcome() == SearchOutcome.SEARCHING) {
            banner.setVisible(false);
            return;
        }
        boolean finalStep = driver.indexProperty().get() == driver.sizeProperty().get() - 1;
        if (current.outcome() == SearchOutcome.NO_PATH) {
            banner.setText("No path found — the target is unreachable");
            banner.getStyleClass().setAll("label", "chip", "warning");
            banner.setVisible(true);
        } else if (finalStep) {
            banner.setText("Path found · " + current.metrics().get(MetricType.PATH_LENGTH) + " moves · cost "
                    + current.metrics().get(MetricType.PATH_COST) + " · " + current.metrics().get(MetricType.NODES_EXPLORED)
                    + " nodes explored");
            banner.getStyleClass().setAll("label", "chip", "success");
            banner.setVisible(true);
        } else {
            banner.setVisible(false);
        }
    }

    // ------------------------------------------------------------- module

    @Override
    public void onShow(NavigationRequest request) {
        canvas.setShowDistances(context.settings().showGridDistances());
        if (request.algorithmId() != null) {
            context.catalog().graphById(request.algorithmId()).ifPresent(a -> {
                algorithmBox.getSelectionModel().select(a);
                updateAlgorithmInfo();
            });
        }
        if (request.algorithmId() != null) {
            rebuild();
        }
    }

    @Override
    public List<Node> headerActions() {
        Button learn = new Button("Learn this algorithm");
        learn.setGraphic(Icons.create(IconType.LIBRARY, 15));
        learn.getStyleClass().add("ghost");
        learn.setOnAction(e -> context.navigate(ModuleId.LIBRARY, NavigationRequest.select(selected().metadata().id())));
        return List.of(learn);
    }

    @Override
    public String helpText() {
        return "Draw walls by clicking or dragging on the grid (right-click erases). Use the tool buttons to place "
                + "weighted cells or move the start (S) and target (T). Press Next (→) to step through the search: teal cells "
                + "wait in the frontier, indigo cells have been visited, the amber cell is being expanded, and the "
                + "green cells form the final path. Dijkstra shows each cell's best known distance. Editing the grid "
                + "restarts the run.";
    }
}
