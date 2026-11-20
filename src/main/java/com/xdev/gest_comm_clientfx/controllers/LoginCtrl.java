package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
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
            statusLabel.setText(
                "Saisissez votre nom d'utilisateur et votre mot de passe.");
            usernameField.requestFocus();
            return;
        }
        if (baseUrl.isEmpty()) {
            statusLabel.setText("Saisissez l'adresse du serveur.");
            baseUrlField.requestFocus();
            return;
        }

        setBusy(true);
        statusLabel.setText("Connexion…");
        new Thread(() -> {
            try {
                UserProfile profile =
                    new ApiClient(config).signIn(baseUrl, username, password);
                Platform.runLater(() -> {
                    setBusy(false);
                    statusLabel.setText("Connecté : " + profile.getDisplayName());
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
            return "Nom d'utilisateur ou mot de passe incorrect.";
        }
        if (e.getStatusCode() == 0) {
            return "Connexion impossible : " + e.getDetail()
                + " — vérifiez l'adresse du serveur.";
        }
        return "Connexion refusée : " + e.getDetail();
    }

    private void setBusy(boolean busy) {
        signInButton.setDisable(busy);
        usernameField.setDisable(busy);
        passwordField.setDisable(busy);
        baseUrlField.setDisable(busy);
    }
}
