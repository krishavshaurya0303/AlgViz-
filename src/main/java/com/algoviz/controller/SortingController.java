package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.sorting.InsertionSort;
import com.algoviz.engine.sorting.MergeSort;
import com.algoviz.engine.sorting.SortInput;
import com.algoviz.engine.sorting.SortOrder;
import com.algoviz.engine.sorting.SortStep;
import com.algoviz.engine.sorting.SortingAlgorithm;
import com.algoviz.model.AlgorithmMetadata;
import com.algoviz.model.MetricType;
import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import com.algoviz.ui.render.SortingCanvas;
import com.algoviz.ui.theme.Palette;
import com.algoviz.util.ComplexityUtils;
import com.algoviz.util.InvalidInputException;
import com.algoviz.util.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Sorting Studio: input configuration, trace generation and bar rendering. */
public final class SortingController extends VisualizerController<SortStep> {

    /** Shapes of generated input. */
    enum Pattern {
        RANDOM("Random"), NEARLY_SORTED("Nearly sorted"), REVERSED("Reversed"),
        SORTED("Already sorted"), FEW_UNIQUE("Few unique values");

        private final String label;

        Pattern(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    @FXML private HBox toolbar;
    @FXML private ComboBox<SortingAlgorithm> algorithmBox;
    @FXML private ComboBox<SortOrder> orderBox;
    @FXML private Slider sizeSlider;
    @FXML private Label sizeLabel;
    @FXML private ComboBox<Pattern> patternBox;
    @FXML private Button generateButton;
    @FXML private TextField customField;
    @FXML private Button applyButton;
    @FXML private Label inputMessage;
    @FXML private SortingCanvas canvas;
    @FXML private Label buildNote;
    @FXML private Label orderNote;
    @FXML private Label aboutLabel;

    private final Random random = new Random();
    private int[] input = new int[0];

    public SortingController(AppContext context) {
        super(context);
    }

    @FXML
    private void initialize() {
        algorithmBox.getItems().setAll(context.catalog().sortingAlgorithms());
        algorithmBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(SortingAlgorithm a) {
                return a == null ? "" : a.name();
            }

            @Override
            public SortingAlgorithm fromString(String s) {
                return null;
            }
        });
        algorithmBox.setButtonCell(nameCell());
        algorithmBox.getSelectionModel().selectFirst();
        algorithmBox.setTooltip(new Tooltip("Sorting strategy to visualize"));

        orderBox.getItems().setAll(SortOrder.values());
        orderBox.getSelectionModel().selectFirst();

        sizeSlider.setMin(AppConfig.SORT_MIN_SIZE);
        sizeSlider.setMax(AppConfig.SORT_MAX_SIZE);
        sizeSlider.setValue(context.settings().defaultArraySize());
        sizeSlider.setBlockIncrement(1);
        sizeSlider.setMajorTickUnit(1);
        sizeSlider.setMinorTickCount(0);
        sizeSlider.setSnapToTicks(true);
        sizeSlider.setTooltip(new Tooltip("Number of elements (" + AppConfig.SORT_MIN_SIZE + "–"
                + AppConfig.SORT_MAX_SIZE + "). Small arrays are best for learning."));
        updateSizeLabel();
        sizeSlider.valueProperty().addListener((o, a, v) -> {
            updateSizeLabel();
            if (!sizeSlider.isValueChanging()) {
                generate();
            }
        });
        sizeSlider.valueChangingProperty().addListener((o, a, changing) -> {
            if (!changing) {
                generate();
            }
        });

        patternBox.getItems().setAll(Pattern.values());
        patternBox.getSelectionModel().selectFirst();
        patternBox.setOnAction(e -> generate());

        generateButton.setGraphic(Icons.create(IconType.SHUFFLE, 15));
        generateButton.setTooltip(new Tooltip("Create a new array with the chosen size and pattern"));
        customField.setOnAction(e -> applyCustom());
        customField.textProperty().addListener((o, a, v) -> customField.getStyleClass().remove("invalid"));

        legend.item(Palette.NEUTRAL, "Unsorted")
                .item(Palette.CORAL, "Comparing")
                .item(Palette.AMBER, "Swap / write")
                .item(Palette.VIOLET, "Pivot")
                .item(Palette.CYAN, "Minimum / key")
                .item(Palette.GREEN, "Final position");

        metricsPanel.configure(List.of(MetricType.COMPARISONS, MetricType.SWAPS, MetricType.WRITES),
                List.<String[]>of(new String[]{STEP, "Position in the recorded sequence of steps."}));

        algorithmBox.setOnAction(e -> {
            updateAlgorithmInfo();
            rebuild();
            context.recordActivity(ModuleId.SORTING, selected().metadata().id());
        });
        orderBox.setOnAction(e -> {
            updateAlgorithmInfo();
            rebuild();
        });

        initPlayback();
        updateAlgorithmInfo();
        generate();
    }

    private ListCell<SortingAlgorithm> nameCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(SortingAlgorithm item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.name());
            }
        };
    }

    private SortingAlgorithm selected() {
        return algorithmBox.getValue();
    }

    private void updateSizeLabel() {
        sizeLabel.setText("Size · " + (int) Math.round(sizeSlider.getValue()));
    }

    private void updateAlgorithmInfo() {
        AlgorithmMetadata m = selected().metadata();
        pseudocodePanel.setCode(m.pseudocode());
        complexityCard.show(m);
        aboutLabel.setText(m.summary() + "\n\nUseful for: " + m.whenUseful());
        boolean desc = orderBox.getValue() == SortOrder.DESCENDING;
        orderNote.setText(desc ? "Descending order: every comparison shown as \">\" or \"<=\" is reversed."
                : "Pseudocode is written for ascending order.");
        canvas.setReserveBuffer(selected() instanceof MergeSort);
        canvas.setReserveKeyBand(selected() instanceof InsertionSort);
    }

    // ------------------------------------------------------------- inputs

    @FXML
    private void generate() {
        int n = (int) Math.round(sizeSlider.getValue());
        input = generate(patternBox.getValue(), n);
        customField.clear();
        inputMessage.setText("");
        inputMessage.getStyleClass().removeAll("error", "success");
        rebuild();
    }

    private int[] generate(Pattern pattern, int n) {
        int lo = AppConfig.SORT_RANDOM_MIN;
        int hi = AppConfig.SORT_RANDOM_MAX;
        int[] a = new int[n];
        switch (pattern) {
            case FEW_UNIQUE -> {
                int[] pool = {lo + (hi - lo) / 5, lo + (hi - lo) / 2, lo + 4 * (hi - lo) / 5, hi};
                for (int i = 0; i < n; i++) {
                    a[i] = pool[random.nextInt(pool.length)];
                }
            }
            default -> {
                for (int i = 0; i < n; i++) {
                    a[i] = lo + random.nextInt(hi - lo + 1);
                }
            }
        }
        if (pattern == Pattern.SORTED || pattern == Pattern.NEARLY_SORTED || pattern == Pattern.REVERSED) {
            Arrays.sort(a);
        }
        if (pattern == Pattern.REVERSED) {
            for (int i = 0, j = n - 1; i < j; i++, j--) {
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        if (pattern == Pattern.NEARLY_SORTED) {
            int swaps = Math.max(1, n / 10);
            for (int k = 0; k < swaps; k++) {
                int i = random.nextInt(n);
                int j = Math.min(n - 1, i + 1 + random.nextInt(2));
                int t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        return a;
    }

    @FXML
    private void applyCustom() {
        try {
            int[] values = ValidationUtils.parseIntList(customField.getText(), AppConfig.SORT_MIN_SIZE,
                    AppConfig.SORT_MAX_SIZE, AppConfig.SORT_MIN_VALUE, AppConfig.SORT_MAX_VALUE);
            input = values;
            inputMessage.getStyleClass().removeAll("error");
            inputMessage.getStyleClass().add("success");
            inputMessage.setText("Using your " + values.length + " values.");
            rebuild();
        } catch (InvalidInputException e) {
            customField.getStyleClass().add("invalid");
            inputMessage.getStyleClass().removeAll("success");
            inputMessage.getStyleClass().add("error");
            inputMessage.setText(e.getMessage() + " Allowed: " + AppConfig.SORT_MIN_SIZE + "–" + AppConfig.SORT_MAX_SIZE
                    + " values between " + AppConfig.SORT_MIN_VALUE + " and " + AppConfig.SORT_MAX_VALUE + ".");
        }
    }

    /** Records a fresh trace for the current input in the background and loads it. */
    private void rebuild() {
        SortingAlgorithm algorithm = selected();
        SortInput in = new SortInput(input, orderBox.getValue());
        int min = Arrays.stream(input).min().orElse(0);
        int max = Arrays.stream(input).max().orElse(1);
        canvas.setValueRange(min, max);
        driver.suspend();
        buildNote.setText("Recording steps…");
        runner.submit(() -> algorithm.run(in), this::onTraceReady, error -> {
            explanationCard.show("Could not run " + algorithm.name() + ": " + error.getMessage(), StepStatus.FAILURE);
            buildNote.setText("");
        });
    }

    private void onTraceReady(ExecutionTrace<SortStep> trace) {
        driver.load(trace);
        buildNote.setText(trace.size() + " steps recorded in " + ComplexityUtils.formatBuildTime(trace.buildNanos())
                + " (includes snapshot recording — not a performance benchmark).");
    }

    // ------------------------------------------------------------- rendering

    @Override
    protected void render(SortStep previous, SortStep current, double millis) {
        if (current == null) {
            return;
        }
        canvas.show(previous, current, millis);
    }

    // ------------------------------------------------------------- module

    @Override
    public void onShow(NavigationRequest request) {
        if (request.algorithmId() != null) {
            context.catalog().sortingById(request.algorithmId()).ifPresent(a -> {
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
        return "Choose an algorithm, an order and an input, then press Next (→) to step through. Every bar is one array element. "
                + "Coral bars are being compared, amber bars are being swapped or written, and green bars have "
                + "reached their final position. Step backward restores the exact earlier state, including the "
                + "counters. Merge Sort shows its temporary buffer below the array; Insertion Sort lifts the key "
                + "above the array while larger elements shift right.";
    }
}
