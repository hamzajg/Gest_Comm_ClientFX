package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.Article;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class NouvelArticleCtrl implements ApiDialog {

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
    private Article editing;

    /** Prefill the form for editing an existing article (PUT on save). */
    public void setEditing(Article article) {
        this.editing = article;
        refArticleField.setText(article.getRef_article());
        descArticleField.setText(article.getDesc_article());
        prixAchatField.setText(textOr(article.getPrix_achat_unitaire()));
        prixVenteField.setText(textOr(article.getPrix_vente_unitaire()));
        remiseAchatField.setText(textOr(article.getRemise_achat()));
        remiseVenteField.setText(textOr(article.getRemise_vente()));
        tvaAchatField.setText(textOr(article.getTva_achat()));
        tvaVenteField.setText(textOr(article.getTva_vente()));
        dernierPrixField.setText(
            textOr(article.getDernier_prix_vente_unitaire()));
    }

    private static String textOr(Object value) {
        return Display.text(value);
    }

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

        statusLabel.setText(Messages.get("form.saving"));
        new Thread(() -> {
            try {
                if (editing != null) {
                    api.put("/api/article/" + editing.getId_article(),
                        payload, Article.class);
                } else {
                    api.post("/api/article", payload, Article.class);
                }
                Platform.runLater(() -> {
                    if (onSaved != null) {
                        onSaved.run();
                    }
                    closeWindow();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> statusLabel.setText(
                    ApiMessages.refused(Messages.get("record.action.save"), e)));
            }
        }).start();
    }

    private String validate() {
        if (refArticleField.getText().isBlank()) {
            return Messages.get("form.error.reference");
        }
        if (descArticleField.getText().isBlank()) {
            return Messages.get("form.error.designation");
        }
        if (prixVenteField.getText().isBlank()
                || !isDecimal(prixVenteField.getText())) {
            return Messages.get("form.error.salePrice");
        }
        for (TextField f : new TextField[]{prixAchatField, remiseAchatField,
                remiseVenteField, tvaAchatField, tvaVenteField,
                dernierPrixField}) {
            if (!f.getText().isBlank() && !isDecimal(f.getText())) {
                return Messages.get("form.error.number", f.getText());
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

    private Runnable onClosed;

    @Override
    public void setOnClosed(Runnable onClosed) {
        this.onClosed = onClosed;
    }

    private void closeWindow() {
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
