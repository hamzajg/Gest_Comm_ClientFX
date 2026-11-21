package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.UserProfile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * The gate in front of the workspace (specs/login-profile-settings D2).
 *
 * The workspace is only built after `/api/token` accepts the credentials and
 * `/api/me` returns the identity, so nothing of the commercial data exists on
 * screen until someone has signed in.
 */
public class LoginCtrl {

    @FXML private TextField baseUrlField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button signInButton;
    @FXML private Label statusLabel;

    private final ApiConfig config = ApiConfig.load();
    private Runnable onSignedIn;

    @FXML
    public void initialize() {
        baseUrlField.setText(config.getBaseUrl());
        usernameField.setText(config.getUsername());
        passwordField.setOnAction(e -> signIn());
    }

    /** Called on the FX thread once a session exists. */
    public void setOnSignedIn(Runnable onSignedIn) {
        this.onSignedIn = onSignedIn;
    }

    @FXML
    public void signIn() {
        String baseUrl = baseUrlField.getText() == null
            ? "" : baseUrlField.getText().trim();
        String username = usernameField.getText() == null
            ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null
            ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText(Messages.get("login.missing"));
            usernameField.requestFocus();
            return;
        }
        if (baseUrl.isEmpty()) {
            statusLabel.setText(Messages.get("login.missingServer"));
            baseUrlField.requestFocus();
            return;
        }

        setBusy(true);
        statusLabel.setText(Messages.get("login.connecting"));
        new Thread(() -> {
            try {
                UserProfile profile =
                    new ApiClient(config).signIn(baseUrl, username, password);
                Platform.runLater(() -> {
                    setBusy(false);
                    statusLabel.setText(Messages.get("login.connected",
                        profile.getDisplayName()));
                    if (onSignedIn != null) {
                        onSignedIn.run();
                    }
                });
            } catch (ApiException e) {
                Platform.runLater(() -> {
                    setBusy(false);
                    passwordField.clear();
                    passwordField.requestFocus();
                    statusLabel.setText(message(e));
                });
            }
        }).start();
    }

    /** Refusal wording: the backend's own message when it gave one. */
    static String message(ApiException e) {
        if (e.getStatusCode() == 401) {
            return Messages.get("login.refused");
        }
        if (e.getStatusCode() == 0) {
            return Messages.get("login.unreachable", e.getDetail());
        }
        return Messages.get("login.denied", e.getDetail());
    }

    private void setBusy(boolean busy) {
        signInButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        baseUrlField.setDisable(busy);
    }
}
