package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.models.Article;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class NouvelArticleCtrl {

    @FXML private TextField refArticleField;
    @FXML private TextField descArticleField;
    @FXML private TextField prixAchatField;
    @FXML private TextField prixVenteField;
    @FXML private TextField remiseAchatField;
    @FXML private TextField remiseVenteField;
    @FXML private TextField tvaAchatField;
    @FXML private TextField tvaVenteField;
    @FXML private TextField dernierPrixField;
    @FXML private Label statusLabel;

    private ApiClient api;
    private Runnable onSaved;

    public void setApi(ApiClient api) {
        this.api = api;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    public void save() {
        String error = validate();
        if (error != null) {
            statusLabel.setText(error);
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("refArticle", refArticleField.getText().trim());
        payload.put("descArticle", descArticleField.getText().trim());
        putDecimal(payload, "prixAchatUnitaire", prixAchatField.getText());
        putDecimal(payload, "prixVenteUnitaire", prixVenteField.getText());
        putDecimal(payload, "remiseAchat", remiseAchatField.getText());
        putDecimal(payload, "remiseVente", remiseVenteField.getText());
        putDecimal(payload, "tvaAchat", tvaAchatField.getText());
        putDecimal(payload, "tvaVente", tvaVenteField.getText());
        putDecimal(payload, "dernierPrixVenteUnitaire",
            dernierPrixField.getText());

        statusLabel.setText("Enregistrement…");
        new Thread(() -> {
            try {
                api.post("/api/article", payload, Article.class);
                Platform.runLater(() -> {
                    if (onSaved != null) {
                        onSaved.run();
                    }
                    closeWindow();
                });
            } catch (ApiException e) {
                Platform.runLater(() ->
                    statusLabel.setText("Erreur : " + e.getMessage()));
            }
        }).start();
    }

    private String validate() {
        if (refArticleField.getText().isBlank()) {
            return "La référence est obligatoire.";
        }
        if (descArticleField.getText().isBlank()) {
            return "La désignation est obligatoire.";
        }
        if (prixVenteField.getText().isBlank()
                || !isDecimal(prixVenteField.getText())) {
            return "Le prix de vente est obligatoire (nombre).";
        }
        for (TextField f : new TextField[]{prixAchatField, remiseAchatField,
                remiseVenteField, tvaAchatField, tvaVenteField,
                dernierPrixField}) {
            if (!f.getText().isBlank() && !isDecimal(f.getText())) {
                return "Valeur numérique invalide : " + f.getText();
            }
        }
        return null;
    }

    private static boolean isDecimal(String text) {
        return text.trim().matches("-?\\d+(\\.\\d+)?");
    }

    private static void putDecimal(Map<String, Object> payload,
                                  String key, String text) {
        String t = text.trim();
        if (!t.isEmpty()) {
            payload.put(key, t); // backend DecimalField/FloatField accept strings
        }
    }

    @FXML
    public void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
