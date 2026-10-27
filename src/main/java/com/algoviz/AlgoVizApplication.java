package com.algoviz;

import com.algoviz.config.AppConfig;
import com.algoviz.controller.AppContext;
import com.algoviz.controller.MainController;
import com.algoviz.model.UserSettings;
import com.algoviz.service.AlgorithmCatalog;
import com.algoviz.service.SettingsService;
import com.algoviz.ui.theme.FontLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** JavaFX entry point: wires services, loads the shell and shows the window. */
public final class AlgoVizApplication extends Application {

    private static final Logger LOG = Logger.getLogger(AlgoVizApplication.class.getName());
    private AppContext context;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        installCrashGuard();
        FontLoader.loadBundledFonts();

        SettingsService settingsService = SettingsService.createDefault();
        UserSettings settings = settingsService.load();
        context = new AppContext(new AlgorithmCatalog(), settingsService, settings);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/algoviz/fxml/MainView.fxml"));
        loader.setControllerFactory(type -> MainController.createController(type, context));
        Parent root = loader.load();
        MainController main = loader.getController();

        Scene scene = new Scene(root, 1440, 900);
        for (String css : new String[]{"app.css", "components.css", "modules.css"}) {
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/com/algoviz/css/" + css)).toExternalForm());
        }
        main.attach(scene);

        stage.setTitle(AppConfig.APP_NAME + " — " + AppConfig.APP_FULL_NAME);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.show();
        main.openStartModule();
        if (settingsService.lastError() != null) {
            context.notify(settingsService.lastError(), com.algoviz.model.StepStatus.WARNING);
        }
    }

    @Override
    public void stop() {
        if (context != null) {
            context.saveSettings();
        }
    }

    /**
     * Unexpected exceptions on the FX thread are logged and reported in a dialog
     * instead of silently breaking the UI.
     */
    private static void installCrashGuard() {
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            LOG.log(Level.SEVERE, "Unexpected error", e);
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("AlgoViz");
                a.setHeaderText("Something went wrong");
                a.setContentText("An unexpected error occurred: " + e
                        + "\nThe current operation was stopped; you can keep using the application.");
                a.show();
            });
        });
    }
}
