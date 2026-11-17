package com.xdev.gest_comm_clientfx.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Map;

public class AssistantCtrl implements ApiDialog {

    @FXML private TextField questionField;
    @FXML private TextArea logArea;
    @FXML private Label statusLabel;

    private ApiClient api;
    private Runnable onSaved;
    private final ObjectMapper mapper = new ObjectMapper();

    public void setApi(ApiClient api) {
        this.api = api;
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    public void initialize() {
        questionField.setOnAction(e -> ask());
    }

    @FXML
    public void ask() {
        String question = questionField.getText().trim();
        if (question.isEmpty()) {
            return;
        }
        questionField.clear();
        statusLabel.setText("Réflexion…");
        new Thread(() -> {
            try {
                Map<String, Object> response = api.askAssistant(question);
                Platform.runLater(() -> {
                    appendExchange(question, response);
                    statusLabel.setText("");
                });
            } catch (ApiException e) {
                Platform.runLater(() -> statusLabel.setText(
                    "Erreur : " + e.getMessage()));
            }
        }).start();
    }

    private void appendExchange(String question,
                                Map<String, Object> response) {
        StringBuilder sb = new StringBuilder(logArea.getText());
        sb.append("Q: ").append(question).append('\n');
        sb.append("R: ").append(response.get("answer")).append('\n');
        Object data = response.get("data");
        if (data != null) {
            try {
                sb.append(mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(data)).append('\n');
            } catch (Exception e) {
                sb.append(data).append('\n');
            }
        }
        sb.append('\n');
        logArea.setText(sb.toString());
        logArea.setScrollTop(Double.MAX_VALUE);
    }
}
