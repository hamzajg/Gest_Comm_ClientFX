package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.Client;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class NouveauClientCtrl implements ApiDialog {

    @FXML private TextField codeClientField;
    @FXML private TextField nomPrenomField;
    @FXML private TextField matFiscalField;
    @FXML private TextField adresse1Field;
    @FXML private TextField adresse2Field;
    @FXML private TextField tel1Field;
    @FXML private TextField tel2Field;
    @FXML private Label statusLabel;

    private ApiClient api;
    private Runnable onSaved;
    private Client editing;

    /** Prefill the form for editing an existing client (PUT on save). */
    public void setEditing(Client client) {
        this.editing = client;
        codeClientField.setText(Display.text(client.getCodeClient()));
        nomPrenomField.setText(Display.text(client.getNomPrenomClient()));
        matFiscalField.setText(Display.text(client.getMatFiscalClient()));
        adresse1Field.setText(Display.text(client.getAddresseClient1()));
        adresse2Field.setText(Display.text(client.getAddresseClient2()));
        tel1Field.setText(phoneText(client.getNumTelClient1()));
        tel2Field.setText(phoneText(client.getNumTelClient2()));
    }

    private static String phoneText(Integer phone) {
        return phone != null && phone != 0 ? String.valueOf(phone) : "";
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
        payload.put("codeClient", codeClientField.getText().trim());
        payload.put("nomPrenomClient", nomPrenomField.getText().trim());
        payload.put("matFiscalClient", matFiscalField.getText().trim());
        payload.put("addresseClient1", adresse1Field.getText().trim());
        payload.put("addresseClient2",
            orDefault(adresse2Field.getText().trim(), "-"));
        payload.put("numTelClient1", parsePhone(tel1Field.getText()));
        payload.put("numTelClient2", parsePhone(tel2Field.getText()));

        statusLabel.setText(Messages.get("form.saving"));
        new Thread(() -> {
            try {
                if (editing != null) {
                    api.put("/api/client/" + editing.getIdClient(),
                        payload, Client.class);
                } else {
                    api.post("/api/client", payload, Client.class);
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
        if (codeClientField.getText().isBlank()) {
            return Messages.get("form.error.clientCode");
        }
        if (nomPrenomField.getText().isBlank()) {
            return Messages.get("form.error.clientName");
        }
        if (matFiscalField.getText().isBlank()) {
            return Messages.get("form.error.taxId");
        }
        if (adresse1Field.getText().isBlank()) {
            return Messages.get("form.error.address1");
        }
        if (!isInteger(tel1Field.getText())
                || !isInteger(tel2Field.getText())) {
            return Messages.get("form.error.phones");
        }
        return null;
    }

    private static boolean isInteger(String text) {
        String t = text.trim();
        return t.isEmpty()
            || (t.matches("-?\\d+") && t.length() < 10);
    }

    private static int parsePhone(String text) {
        String t = text.trim();
        return t.isEmpty() ? 0 : Integer.parseInt(t);
    }

    private static String orDefault(String value, String fallback) {
        return value.isEmpty() ? fallback : value;
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
