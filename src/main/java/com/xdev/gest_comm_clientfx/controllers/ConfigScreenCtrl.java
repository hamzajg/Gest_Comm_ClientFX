package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.UserProfile;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Connection settings: the server address only.
 *
 * There is no username or password here any more — the account is entered on
 * the login gate and the password is never persisted (see
 * specs/login-profile-settings D3). "Tester la connexion" asks the backend who
 * the current session belongs to, which also proves the URL is right.
 */
public class ConfigScreenCtrl {

    @FXML private TextField baseUrlField;
    @FXML private Label statusLabel;

    private final ApiConfig config = ApiConfig.load();

    @FXML
    public void initialize() {
        baseUrlField.setText(config.getBaseUrl());
    }

    @FXML
    public void saveConfig() {
        config.setBaseUrl(baseUrlField.getText().trim());
        config.save();
        statusLabel.setText(Messages.get("settings.saved"));
    }

    @FXML
    public void testConnection() {
        config.setBaseUrl(baseUrlField.getText().trim());
        config.save();
        statusLabel.setText(Messages.get("settings.testing"));
        new Thread(() -> {
            try {
                UserProfile profile = new ApiClient(config).profile();
                javafx.application.Platform.runLater(() ->
                    statusLabel.setText(Messages.get("settings.ok",
                        profile.getDisplayName())));
            } catch (ApiException e) {
                javafx.application.Platform.runLater(() ->
                    statusLabel.setText(Messages.get("settings.failed",
                        Display.text(e.getMessage(),
                            Messages.get("settings.unreachable")))));
            }
        }).start();
    }

    private Runnable onClosed;

    public void setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
    }

    @FXML
    public void closeWindow() {
        if (onClosed != null) {
            onClosed.run();
            return;
        }
        if (statusLabel.getScene() == null) {
            return;
        }
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
