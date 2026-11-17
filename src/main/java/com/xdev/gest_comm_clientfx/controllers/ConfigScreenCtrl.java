package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.models.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class ConfigScreenCtrl {

    @FXML private TextField baseUrlField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    private final ApiConfig config = ApiConfig.load();

    @FXML
    public void initialize() {
        baseUrlField.setText(config.getBaseUrl());
        usernameField.setText(config.getUsername());
        passwordField.setText(config.getPassword());
    }

    @FXML
    public void saveConfig() {
        config.setBaseUrl(baseUrlField.getText().trim());
        config.setUsername(usernameField.getText().trim());
        config.setPassword(passwordField.getText());
        config.save();
        statusLabel.setText("Paramètres enregistrés.");
    }

    @FXML
    public void testConnection() {
        config.setBaseUrl(baseUrlField.getText().trim());
        config.setUsername(usernameField.getText().trim());
        config.setPassword(passwordField.getText());
        config.save();
        statusLabel.setText("Test en cours…");
        new Thread(() -> {
            try {
                List<Client> clients = new ApiClient(config).listClients();
                javafx.application.Platform.runLater(() ->
                    statusLabel.setText("Connexion OK — " + clients.size()
                        + " client(s)."));
            } catch (ApiException e) {
                javafx.application.Platform.runLater(() ->
                    statusLabel.setText("Échec : " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
