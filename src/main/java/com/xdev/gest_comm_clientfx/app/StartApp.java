package com.xdev.gest_comm_clientfx.app;

import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.controllers.LoginCtrl;
import com.xdev.gest_comm_clientfx.ui.Theme;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class StartApp extends Application {

    private static final String LOGIN_VIEW =
        "/com/xdev/gest_comm_clientfx/views/LoginView.fxml";

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        primaryStage.setTitle("Gestion Commerciale");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(640);
        // The workspace does not exist until someone has signed in
        // (specs/login-profile-settings D2); a stored refresh token is the
        // only thing that can skip the gate.
        if (hasStoredSession(ApiConfig.load())) {
            showWorkspace();
        } else {
            showLogin();
        }
        primaryStage.show();
    }

    /** A persisted refresh token means this workstation already signed in. */
    public static boolean hasStoredSession(ApiConfig config) {
        String refresh = config.getRefreshToken();
        return refresh != null && !refresh.isBlank();
    }

    /** The login gate: shows who you are before any commercial data exists. */
    public void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_VIEW));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1024, 640);
            Theme.install(scene);
            LoginCtrl ctrl = loader.getController();
            ctrl.setOnSignedIn(this::showWorkspace);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open the login screen", e);
        }
    }

    /** Builds the workspace scene on the stage that the login gate used. */
    public void showWorkspace() {
        try {
            Scene scene = new Scene(new HomeScreen().getRootView(), 1200, 700);
            Theme.install(scene);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open the workspace", e);
        }
    }


    public static void main(String[] args) {
        // French UI: JavaFX's own strings (the table's "no content" caption,
        // dialog buttons) must match the app's language. Safe here — every
        // date in the client is an ISO text field, not a DatePicker.
        Locale.setDefault(Locale.FRANCE);
        launch(args);
    }
}
