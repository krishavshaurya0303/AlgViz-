package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.component.BrandMark;
import com.algoviz.ui.component.IconType;
import com.algoviz.ui.component.Icons;
import com.algoviz.ui.component.NotificationCenter;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Application shell: sidebar navigation, header and lazy loading of module views.
 * Each module is loaded from its own FXML file the first time it is opened and
 * then cached, so switching keeps its state. Leaving a module calls
 * {@link ModuleController#onHide()} which pauses any playback, so a hidden
 * module can never keep updating the screen.
 */
public final class MainController {

    private record Loaded(Parent view, ModuleController controller) {
    }

    private final AppContext context;
    private final Map<ModuleId, Loaded> loaded = new EnumMap<>(ModuleId.class);
    private final Map<ModuleId, ToggleButton> navButtons = new EnumMap<>(ModuleId.class);
    private final ToggleGroup navGroup = new ToggleGroup();
    private final NotificationCenter notifications = new NotificationCenter();
    private ModuleId current;
    private ChangeListener<String> statusListener;
    private ChangeListener<StepStatus> toneListener;

    @FXML private HBox brandBox;
    @FXML private VBox navOverview;
    @FXML private VBox navModules;
    @FXML private VBox navLearn;
    @FXML private VBox navBottom;
    @FXML private Label versionLabel;
    @FXML private Label breadcrumb;
    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private HBox headerActions;
    @FXML private Circle statusDot;
    @FXML private Label statusLabel;
    @FXML private Button helpButton;
    @FXML private StackPane contentHost;

    public MainController(AppContext context) {
        this.context = context;
    }

    /** Controller factory: every controller receives the shared context through its constructor. */
    public static Object createController(Class<?> type, AppContext context) {
        try {
            return type.getConstructor(AppContext.class).newInstance(context);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create controller " + type.getName(), e);
        }
    }

    @FXML
    private void initialize() {
        brandBox.getChildren().add(0, new BrandMark(34));
        versionLabel.setText("Version " + AppConfig.VERSION);
        helpButton.setGraphic(Icons.create(IconType.HELP, 18));
        helpButton.setTooltip(new Tooltip("Help and keyboard shortcuts (F1)"));
        helpButton.setAccessibleText("Help");

        addNav(navOverview, ModuleId.DASHBOARD, IconType.DASHBOARD, "Ctrl+1");
        addNav(navModules, ModuleId.SORTING, IconType.SORTING, "Ctrl+2");
        addNav(navModules, ModuleId.GRAPH, IconType.GRAPH, "Ctrl+3");
        addNav(navModules, ModuleId.TREE, IconType.TREE, "Ctrl+4");
        addNav(navModules, ModuleId.LINKED_LIST, IconType.LINKED_LIST, "Ctrl+5");
        addNav(navLearn, ModuleId.LIBRARY, IconType.LIBRARY, "Ctrl+6");
        addNav(navBottom, ModuleId.SETTINGS, IconType.SETTINGS, "Ctrl+7");

        context.setNavigator(this::open);
        context.setNotifier(notifications::show);
    }

    private void addNav(VBox box, ModuleId id, IconType icon, String shortcut) {
        ToggleButton b = new ToggleButton(id.title());
        b.setGraphic(Icons.create(icon, 18));
        b.getStyleClass().setAll("nav-item");
        b.setToggleGroup(navGroup);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setTooltip(new Tooltip(id.subtitle() + "  (" + shortcut + ")"));
        b.setOnAction(e -> {
            if (current == id) {
                b.setSelected(true); // a nav item cannot be "unselected"
            } else {
                open(id, NavigationRequest.NONE);
            }
        });
        navButtons.put(id, b);
        box.getChildren().add(b);
    }

    /** Adds the toast layer and global keyboard handling once the scene exists. */
    public void attach(Scene scene) {
        Parent root = scene.getRoot();
        StackPane layered = new StackPane();
        scene.setRoot(layered);
        layered.getChildren().addAll(root, notifications);
        StackPane.setAlignment(notifications, javafx.geometry.Pos.BOTTOM_RIGHT);
        notifications.setMaxSize(420, javafx.scene.layout.Region.USE_PREF_SIZE);
        notifications.setPickOnBounds(false);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::onKey);
    }

    public void openStartModule() {
        open(context.settings().startModule(), NavigationRequest.NONE);
    }

    private void onKey(KeyEvent e) {
        if (e.isShortcutDown() && e.getCode().isDigitKey()) {
            int n = e.getCode().getCode() - KeyCode.DIGIT1.getCode();
            ModuleId[] ids = ModuleId.values();
            if (n >= 0 && n < ids.length) {
                open(ids[n], NavigationRequest.NONE);
                e.consume();
            }
            return;
        }
        if (e.getCode() == KeyCode.F1) {
            showHelp();
            e.consume();
            return;
        }
        Node focus = contentHost.getScene() == null ? null : contentHost.getScene().getFocusOwner();
        if (focus instanceof TextInputControl || focus instanceof javafx.scene.control.ComboBoxBase
                || focus instanceof javafx.scene.control.Slider || focus instanceof javafx.scene.control.Spinner
                || current == null) {
            return; // never steal keys from a control the user is operating
        }
        Loaded l = loaded.get(current);
        if (l != null && l.controller().handleKey(e)) {
            e.consume();
        }
    }

    /** Switches to a module, loading it on first use. */
    public void open(ModuleId id, NavigationRequest request) {
        Loaded target;
        try {
            target = loaded.computeIfAbsent(id, this::load);
        } catch (UncheckedIOException | IllegalStateException ex) {
            notifications.show("Could not open " + id.title() + ": " + ex.getMessage(), StepStatus.FAILURE);
            if (current != null) {
                navButtons.get(current).setSelected(true);
            }
            return;
        }
        if (current != null && current != id) {
            loaded.get(current).controller().onHide();
        }
        boolean changed = current != id;
        current = id;
        navButtons.get(id).setSelected(true);

        breadcrumb.setText("AlgoViz  /  " + id.title());
        titleLabel.setText(id.title());
        subtitleLabel.setText(id.subtitle());
        headerActions.getChildren().setAll(target.controller().headerActions());
        bindStatus(target.controller());

        if (changed) {
            contentHost.getChildren().setAll(target.view());
            FadeTransition fade = new FadeTransition(Duration.millis(170), target.view());
            fade.setFromValue(context.settings().animateTransitions() ? 0.25 : 1);
            fade.setToValue(1);
            fade.play();
        }
        target.controller().applySettings();
        target.controller().onShow(request == null ? NavigationRequest.NONE : request);
        if (id.isVisualizer() && changed) {
            context.recordActivity(id, null);
        }
    }

    private Loaded load(ModuleId id) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/algoviz/fxml/" + id.fxml()));
        loader.setControllerFactory(type -> createController(type, context));
        try {
            Parent view = loader.load();
            return new Loaded(view, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void bindStatus(ModuleController c) {
        if (statusListener != null) {
            for (Loaded l : loaded.values()) {
                l.controller().statusText().removeListener(statusListener);
                l.controller().statusTone().removeListener(toneListener);
            }
        }
        statusListener = (o, a, v) -> statusLabel.setText(v);
        toneListener = (o, a, v) -> applyTone(v);
        c.statusText().addListener(statusListener);
        c.statusTone().addListener(toneListener);
        statusLabel.setText(c.statusText().get());
        applyTone(c.statusTone().get());
    }

    private void applyTone(StepStatus tone) {
        statusDot.getStyleClass().removeIf(s -> s.startsWith("tone-"));
        statusDot.getStyleClass().add("tone-" + (tone == null ? "info" : tone.name().toLowerCase()));
    }

    @FXML
    private void showHelp() {
        Loaded l = current == null ? null : loaded.get(current);
        String moduleHelp = l == null ? "" : l.controller().helpText();
        Alert a = new Alert(Alert.AlertType.NONE, "", ButtonType.CLOSE);
        a.setTitle("AlgoViz Help");
        a.setHeaderText(current == null ? "Help" : current.title() + " — help");
        VBox body = new VBox(10);
        if (!moduleHelp.isBlank()) {
            Label text = new Label(moduleHelp);
            text.setWrapText(true);
            text.setMaxWidth(460);
            body.getChildren().add(text);
        }
        body.getChildren().add(new Label("Keyboard shortcuts"));
        String[][] keys = {
                {"Space", "Play / pause"}, {"→", "Step forward"}, {"←", "Step backward"},
                {"R", "Reset to the initial state"}, {"End", "Jump to the final step"},
                {"Ctrl+1 … 7", "Switch module"}, {"F1", "This help"}};
        for (String[] k : keys) {
            Label key = new Label(k[0]);
            key.getStyleClass().add("help-key");
            key.setMinWidth(90);
            HBox row = new HBox(12, key, new Label(k[1]));
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            body.getChildren().add(row);
        }
        a.getDialogPane().setContent(body);
        a.getDialogPane().getStylesheets().addAll(contentHost.getScene().getStylesheets());
        a.initOwner(contentHost.getScene().getWindow());
        a.showAndWait();
    }
}
