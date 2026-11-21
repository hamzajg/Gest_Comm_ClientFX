package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.UserProfile;
import com.xdev.gest_comm_clientfx.ui.AppPreferences;
import com.xdev.gest_comm_clientfx.ui.Theme;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.util.Locale;
import java.util.Map;

/**
 * The signed-in account, read from `/api/me` — the client never guesses who
 * the token belongs to (specs/login-profile-settings US3, D1).
 *
 * The card also owns the two local preferences (theme, language) and the
 * password change; signing out revokes the refresh token and hands control
 * back to the shell, which returns to the login scene (D2, US3, D4, D6).
 */
public class ProfileCtrl {

    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Button signOutButton;
    @FXML private ComboBox<String> themeBox;
    @FXML private ComboBox<String> languageBox;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button changePasswordButton;
    @FXML private Label statusLabel;

    private final ApiClient api = new ApiClient(ApiConfig.load());
    private final AppPreferences prefs = AppPreferences.load();
    private Runnable onSignedOut;
    private Runnable onLanguageChanged;

    @FXML
    public void initialize() {
        themeBox.getItems().setAll(
            Messages.get("profile.themeDark"), Messages.get("profile.themeLight"));
        themeBox.getSelectionModel().select(
            prefs.skin() == Theme.Skin.LIGHT ? 1 : 0);
        themeBox.setOnAction(e -> applyTheme());

        languageBox.getItems().setAll(
            Messages.get("profile.languageFr"), Messages.get("profile.languageEn"));
        languageBox.getSelectionModel().select(
            "en".equals(prefs.language()) ? 1 : 0);
        languageBox.setOnAction(e -> applyLanguage());

        loadProfile();
    }

    /** Called on the FX thread once the session has been revoked. */
    public void setOnSignedOut(Runnable onSignedOut) {
        this.onSignedOut = onSignedOut;
    }

    /** The shell rebuilds the scene so the new language applies (D6). */
    public void setOnLanguageChanged(Runnable onLanguageChanged) {
        this.onLanguageChanged = onLanguageChanged;
    }

    /** Fills the card; also used by the capture tool to pose the screen. */
    public void show(UserProfile profile) {
        nameLabel.setText(profile.getDisplayName());
        usernameLabel.setText(Display.text(profile.getUsername()));
        emailLabel.setText(Display.cell(profile.getEmail()));
        roleLabel.setText(Messages.get(profile.isAdmin()
            ? "profile.roleAdmin" : "profile.roleOperator"));
        roleLabel.getStyleClass().removeAll("role-admin", "role-operator");
        roleLabel.getStyleClass().add(
            profile.isAdmin() ? "role-admin" : "role-operator");
    }

    private void loadProfile() {
        statusLabel.setText(Messages.get("profile.loading"));
        new Thread(() -> {
            try {
                UserProfile profile = api.profile();
                Platform.runLater(() -> {
                    show(profile);
                    statusLabel.setText("");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> statusLabel.setText(
                    Messages.get("profile.unavailable", e.getDetail())));
            }
        }).start();
    }

    // --- local preferences -------------------------------------------

    private void applyTheme() {
        Theme.Skin skin = themeBox.getSelectionModel().getSelectedIndex() == 1
            ? Theme.Skin.LIGHT : Theme.Skin.DARK;
        prefs.setSkin(skin);
        Scene scene = themeBox.getScene();
        if (scene != null) {
            // Instant, on the live scene; the shell re-applies it on restart.
            Theme.applySkin(scene, skin);
        }
    }

    private void applyLanguage() {
        String language = languageBox.getSelectionModel().getSelectedIndex() == 1
            ? "en" : "fr";
        if (language.equals(prefs.language())) {
            return;
        }
        prefs.setLanguage(language);
        Locale.setDefault(Messages.locale());
        if (onLanguageChanged != null) {
            onLanguageChanged.run();
        }
    }

    // --- password ----------------------------------------------------

    @FXML
    public void changePassword() {
        String current = text(currentPasswordField);
        String next = text(newPasswordField);
        String confirm = text(confirmPasswordField);

        String problem = validationProblem(current, next, confirm);
        if (problem != null) {
            statusLabel.setText(Messages.get(problem));
            return;
        }

        changePasswordButton.setDisable(true);
        statusLabel.setText(Messages.get("password.changing"));
        new Thread(() -> {
            try {
                api.send("POST", "/api/me/password", Map.of(
                    "currentPassword", current,
                    "newPassword", next));
                Platform.runLater(() -> {
                    changePasswordButton.setDisable(false);
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    statusLabel.setText(Messages.get("password.changed"));
                });
            } catch (ApiException e) {
                // Django's validators explain a rejected password; show it.
                Platform.runLater(() -> {
                    changePasswordButton.setDisable(false);
                    statusLabel.setText(Messages.get("password.refused",
                        e.getDetail()));
                });
            }
        }).start();
    }

    @FXML
    public void signOut() {
        signOutButton.setDisable(true);
        statusLabel.setText(Messages.get("profile.signingOut"));
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

    private static String text(PasswordField field) {
        return field.getText() == null ? "" : field.getText();
    }

    /**
     * The message key for an unusable form, or null when it can be sent. The
     * backend still validates the new password (length, similarity); this is
     * only about not sending an obviously incomplete request.
     */
    static String validationProblem(String current, String next,
                                    String confirm) {
        if (current.isEmpty() || next.isEmpty()) {
            return "password.missing";
        }
        if (!next.equals(confirm)) {
            return "password.mismatch";
        }
        return null;
    }
}
