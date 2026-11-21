package com.xdev.gest_comm_clientfx.app;

import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.controllers.LoginCtrl;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.ui.AppPreferences;
import com.xdev.gest_comm_clientfx.ui.Theme;
import com.xdev.gest_comm_clientfx.ui.Views;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class StartApp extends Application {

    private Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        stage = primaryStage;
        primaryStage.setTitle(Messages.get("app.title"));
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
            FXMLLoader loader = Views.loader(getClass(), "LoginView.fxml");
            Parent root = loader.load();
            Scene scene = new Scene(root, 1024, 640);
            Theme.install(scene, AppPreferences.load().skin());
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
            HomeScreen home = new HomeScreen();
            // Sign-out is the same scene swap as a launch without a session:
            // the workspace is discarded, the gate is rebuilt (US3, D2).
            home.setOnSignedOut(this::signOut);
            // A language change reloads this scene with the new bundle (D6).
            home.setOnLanguageChanged(this::showWorkspace);
            Scene scene = new Scene(home.getRootView(), 1200, 700);
            Theme.install(scene, AppPreferences.load().skin());
            stage.setScene(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open the workspace", e);
        }
    }

    /** The session has already been revoked; show the gate again. */
    public void signOut() {
        showLogin();
    }

    public static void main(String[] args) {
        // The chosen language governs JavaFX's own strings too (table
        // placeholder, dialog buttons). Every date in the client is an ISO
        // text field, not a DatePicker, so the locale is safe to switch.
        Locale.setDefault(Messages.locale());
        launch(args);
    }
}
