package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.linkedlist.ListOperation;
import com.algoviz.engine.tree.TreeOperation;
import com.algoviz.model.ModuleId;
import com.algoviz.model.UserSettings;
import com.algoviz.service.AlgorithmCatalog;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** First screen: welcome, module entry points, honest summary and (real) recent activity. */
public final class DashboardController extends StaticPageController {

    @FXML private Button startButton;
    @FXML private GridPane summaryGrid;
    @FXML private HBox continueCard;
    @FXML private Label continueText;
    @FXML private GridPane moduleGrid;
    @FXML private VBox exploreList;
    @FXML private FlowPane footerChips;

    public DashboardController(AppContext context) {
        super(context);
        status.set(AppConfig.APP_NAME + " " + AppConfig.VERSION + "  ·  works offline");
    }

    @FXML
    private void initialize() {
        startButton.setGraphic(Icons.create(IconType.PLAY, 14));
        AlgorithmCatalog c = context.catalog();
        summary(0, 0, String.valueOf(c.sortingAlgorithms().size()), "sorting algorithms");
        summary(1, 0, String.valueOf(c.graphAlgorithms().size()), "graph searches");
        summary(0, 1, String.valueOf(TreeOperation.values().length - 1), "BST operations");
        summary(1, 1, String.valueOf(ListOperation.values().length), "linked-list operations");

        for (int i = 0; i < 2; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(50);
            cc.setHgrow(Priority.ALWAYS);
            moduleGrid.getColumnConstraints().add(cc);
        }
        moduleGrid.add(moduleCard(ModuleId.SORTING, IconType.SORTING, "sorting",
                "Bars, comparisons and swaps. Watch the pivot partition, the merge buffer fill and the key slide into place.",
                c.sortingAlgorithms().stream().map(a -> a.name()).toList()), 0, 0);
        moduleGrid.add(moduleCard(ModuleId.GRAPH, IconType.GRAPH, "graph",
                "Draw walls and weighted cells, then watch the frontier expand and the shortest path appear.",
                c.graphAlgorithms().stream().map(a -> a.name()).toList()), 1, 0);
        moduleGrid.add(moduleCard(ModuleId.TREE, IconType.TREE, "tree",
                "Insert, search and delete in a binary search tree — including the two-children deletion case.",
                List.of("Insert", "Search", "Delete", "4 traversals")), 0, 1);
        moduleGrid.add(moduleCard(ModuleId.LINKED_LIST, IconType.LINKED_LIST, "linkedlist",
                "Follow head, current, prev and next as nodes are linked, unlinked and reversed.",
                List.of("Insert", "Delete", "Search", "Reverse")), 1, 1);

        explore(IconType.STEP_BACK, "True step backward",
                "Each step is an immutable snapshot, so going back restores the exact array, highlights and counters.");
        explore(IconType.INFO, "Explanations tied to real events",
                "Every sentence is produced by the algorithm at the moment the operation happens.");
        explore(IconType.SORTING, "Honest metrics",
                "Comparisons, swaps, writes, nodes explored and pointer changes are counted by the engine. "
                        + "Complexities are labelled as theory, not measurements.");
        explore(IconType.LIBRARY, "Algorithm library",
                "Pseudocode, use cases and best / average / worst-case tables for every algorithm.");

        for (String chip : new String[]{"Java 17+", "JavaFX", "Strategy pattern", "Observer pattern", "MVC",
                "Immutable step snapshots", "JUnit 5 tested"}) {
            Label l = new Label(chip);
            l.getStyleClass().add("chip");
            footerChips.getChildren().add(l);
        }
    }

    private void summary(int col, int row, String number, String label) {
        Label n = new Label(number);
        n.getStyleClass().add("summary-number");
        Label l = new Label(label);
        l.getStyleClass().add("summary-label");
        summaryGrid.add(new VBox(2, n, l), col, row);
    }

    private VBox moduleCard(ModuleId id, IconType icon, String styleKey, String text, List<String> tags) {
        StackPane iconBox = new StackPane(Icons.create(icon, 20));
        iconBox.getStyleClass().addAll("module-icon", styleKey);
        Label title = new Label(id.title());
        title.getStyleClass().add("module-card-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox head = new HBox(12, iconBox, title, spacer, Icons.create(IconType.ARROW_RIGHT, 16));
        head.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label body = new Label(text);
        body.getStyleClass().add("module-card-text");
        body.setMinHeight(Region.USE_PREF_SIZE);
        FlowPane chips = new FlowPane(6, 6);
        for (String t : tags) {
            Label chip = new Label(t);
            chip.getStyleClass().add("chip");
            chips.getChildren().add(chip);
        }
        VBox card = new VBox(head, body, chips);
        card.getStyleClass().add("module-card");
        card.setFocusTraversable(true);
        card.setAccessibleText("Open " + id.title());
        card.setOnMouseClicked(e -> context.navigate(id));
        card.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                context.navigate(id);
                e.consume();
            }
        });
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void explore(IconType icon, String title, String text) {
        Label t = new Label(title);
        t.getStyleClass().add("settings-name");
        Label d = new Label(text);
        d.getStyleClass().add("settings-desc");
        d.setWrapText(true);
        VBox body = new VBox(2, t, d);
        HBox.setHgrow(body, Priority.ALWAYS);
        HBox row = new HBox(12, Icons.create(icon, 18), body);
        exploreList.getChildren().add(row);
    }

    @Override
    public void onShow(NavigationRequest request) {
        UserSettings s = context.settings();
        boolean has = s.lastModule() != null && s.lastModule().isVisualizer();
        continueCard.setVisible(has);
        continueCard.setManaged(has);
        if (has) {
            String algo = s.lastAlgorithmId() == null ? ""
                    : context.catalog().byId(s.lastAlgorithmId()).map(m -> " — " + m.name()).orElse("");
            continueText.setText("You last opened " + s.lastModule().title() + algo + ago(s.lastOpenedEpochMillis()) + ".");
        }
    }

    private static String ago(long epochMillis) {
        if (epochMillis <= 0) {
            return "";
        }
        Duration d = Duration.between(Instant.ofEpochMilli(epochMillis), Instant.now());
        if (d.isNegative() || d.toMinutes() < 1) {
            return " just now";
        }
        if (d.toHours() < 1) {
            return " " + d.toMinutes() + " min ago";
        }
        if (d.toDays() < 1) {
            return " " + d.toHours() + " h ago";
        }
        return " " + d.toDays() + " day" + (d.toDays() == 1 ? "" : "s") + " ago";
    }

    @FXML
    private void startVisualizing() {
        context.navigate(ModuleId.SORTING);
    }

    @FXML
    private void openLibrary() {
        context.navigate(ModuleId.LIBRARY);
    }

    @FXML
    private void continueLearning() {
        UserSettings s = context.settings();
        if (s.lastModule() != null) {
            context.navigate(s.lastModule(), NavigationRequest.select(s.lastAlgorithmId()));
        }
    }

    @Override
    public String helpText() {
        return "Pick a module from the sidebar or a card below. Your last opened module appears under "
                + "Continue learning.";
    }
}
