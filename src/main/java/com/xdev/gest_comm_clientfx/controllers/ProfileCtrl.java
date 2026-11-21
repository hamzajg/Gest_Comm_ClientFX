package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.models.UserProfile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * The signed-in account, read from `/api/me` — the client never guesses who
 * the token belongs to (specs/login-profile-settings US3, D1).
 *
 * Signing out revokes the refresh token server-side and hands control back
 * to the shell, which returns to the login scene (D2, US3).
 */
public class ProfileCtrl {

    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Button signOutButton;
    @FXML private Label statusLabel;

    private final ApiClient api = new ApiClient(ApiConfig.load());
    private Runnable onSignedOut;

    @FXML
    public void initialize() {
        loadProfile();
    }

    /** Called on the FX thread once the session has been revoked. */
    public void setOnSignedOut(Runnable onSignedOut) {
        this.onSignedOut = onSignedOut;
    }

    /** Fills the card; also used by the capture tool to pose the screen. */
    public void show(UserProfile profile) {
        nameLabel.setText(profile.getDisplayName());
        usernameLabel.setText(Display.text(profile.getUsername()));
        emailLabel.setText(Display.cell(profile.getEmail()));
        roleLabel.setText(profile.getRoleLabel());
        roleLabel.getStyleClass().removeAll("role-admin", "role-operator");
        roleLabel.getStyleClass().add(
            profile.isAdmin() ? "role-admin" : "role-operator");
    }

    private void loadProfile() {
        statusLabel.setText("Chargement du profil…");
        new Thread(() -> {
            try {
                UserProfile profile = api.profile();
                Platform.runLater(() -> {
                    show(profile);
                    statusLabel.setText("");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> statusLabel.setText(
                    "Profil indisponible : " + e.getDetail()));
            }
        }).start();
    }

    @FXML
    public void signOut() {
        signOutButton.setDisable(true);
        statusLabel.setText("Déconnexion…");
        new Thread(() -> {
            // logout() revokes the refresh token and clears local state; it
            // never throws, so the gate is reached even with no server.
            api.logout();
            Platform.runLater(() -> {
                if (onSignedOut != null) {
                    onSignedOut.run();
                }
            });
        }).start();
    }
}
