package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.controllers.NouveauDevisCtrl.LigneDraft;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.BonLiv;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.LigneBonLiv;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NouveauBonLivCtrl implements ApiDialog {

    @FXML private TextField codeBonLivField;
    @FXML private ComboBox<Client> clientCombo;
    @FXML private TextField dateBonLivField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextField observationField;
    @FXML private TableView<LigneDraft> linesTable;
    @FXML private TableColumn<LigneDraft, String> colDesignation;
    @FXML private TableColumn<LigneDraft, String> colQuantite;
    @FXML private TableColumn<LigneDraft, String> colPrix;
    @FXML private TableColumn<LigneDraft, String> colRemise;
    @FXML private TableColumn<LigneDraft, String> colTva;
    @FXML private ComboBox<Article> articleCombo;
    @FXML private TextField quantiteField;
    @FXML private TextField prixField;
    @FXML private TextField remiseField;
    @FXML private TextField tvaField;
    @FXML private TextField designationField;
    @FXML private Label totalsLabel;
    @FXML private Label statusLabel;

    private ApiClient api;
    private boolean lookupsStarted;
    private Runnable onSaved;
    private BonLiv editing;
    private Long pendingClientId;
    private final ObservableList<LigneDraft> lines =
        FXCollections.observableArrayList();
    private List<Client> clients = new ArrayList<>();
    private List<Article> articles = new ArrayList<>();

    /** Prefill the form for editing an existing bon de livraison
     *  (PUT on save; the backend replaces lignes when provided). */
    public void setEditing(BonLiv bonliv) {
        this.editing = bonliv;
        this.pendingClientId = bonliv.getClient();
        codeBonLivField.setText(Display.text(bonliv.getCodeBonLiv()));
        dateBonLivField.setText(textOr(bonliv.getDateBonLiv(), ""));
        observationField.setText(
            textOr(bonliv.getObservationBonLiv(), ""));
        if (bonliv.getStatutBonLiv() != null) {
            statutCombo.getSelectionModel()
                .select(bonliv.getStatutBonLiv());
        }
        lines.clear();
        if (bonliv.getLignes() != null) {
            for (LigneBonLiv l : bonliv.getLignes()) {
                lines.add(new LigneDraft(
                    l.getArticle(), l.getDesignation(),
                    textOr(l.getQuantite(), "1"),
                    textOr(l.getPrixUnitaire(), "0"),
                    textOr(l.getRemise(), "0"),
                    textOr(l.getTva(), "0")));
            }
        }
        updateTotals();
    }

    public void setApi(ApiClient api) {
        this.api = api;
        if (!lookupsStarted) {
            lookupsStarted = true;
            }
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    public void initialize() {
        colDesignation.setCellValueFactory(
            new PropertyValueFactory<>("designation"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colRemise.setCellValueFactory(new PropertyValueFactory<>("remise"));
        colTva.setCellValueFactory(new PropertyValueFactory<>("tva"));
        linesTable.setItems(lines);
        linesTable.setPlaceholder(new Label(Messages.get("line.empty")));
        lines.addListener((javafx.collections.ListChangeListener<LigneDraft>)
            c -> updateTotals());

        dateBonLivField.setText(LocalDate.now().toString());
        statutCombo.getItems().addAll("brouillon", "valide", "livre",
            "annule");
        statutCombo.getSelectionModel().selectFirst();
        quantiteField.setText("1");
        remiseField.setText("0");
        tvaField.setText("0");
        updateTotals();

        articleCombo.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldV, article) -> prefillFromArticle(article));

        statusLabel.setText(Messages.get("form.loading.lookups"));
    }

    private void loadLookups() {
        try {
            clients = api.listClients();
            articles = api.listArticles();
            Platform.runLater(() -> {
                clientCombo.getItems().setAll(clients);
                articleCombo.getItems().setAll(articles);
                if (pendingClientId != null) {
                    for (Client c : clients) {
                        if (pendingClientId.equals(c.getIdClient())) {
                            clientCombo.getSelectionModel().select(c);
                            break;
                        }
                    }
                    pendingClientId = null;
                } else if (!clients.isEmpty()) {
                    clientCombo.getSelectionModel().selectFirst();
                }
                statusLabel.setText("");
            });
        } catch (ApiException e) {
            Platform.runLater(() ->
                statusLabel.setText(ApiMessages.loadFailed(e)));
        }
    }

    private void prefillFromArticle(Article article) {
        if (article == null) {
            return;
        }
        designationField.setText(Display.text(article.getDesc_article()));
        prixField.setText(textOr(article.getPrix_vente_unitaire(), "0"));
        remiseField.setText(textOr(article.getRemise_vente(), "0"));
        tvaField.setText(textOr(article.getTva_vente(), "0"));
    }

    private static String textOr(Object value, String fallback) {
        return Display.text(value, fallback);
    }

    @FXML
    public void addLine() {
        String designation = designationField.getText().trim();
        String quantite = quantiteField.getText().trim();
        String prix = prixField.getText().trim();
        if (designation.isEmpty() && articleCombo.getValue() == null) {
            statusLabel.setText(
                Messages.get("form.chooseArticleOrDesignation"));
            return;
        }
        if (designation.isEmpty()) {
            designation = articleCombo.getValue().getDesc_article();
        }
        if (!isPositiveNumber(quantite) || !isPositiveNumber(prix)) {
            statusLabel.setText(Messages.get("form.positiveNumbers"));
            return;
        }
        Article article = articleCombo.getValue();
        lines.add(new LigneDraft(
            article != null ? article.getId_article() : null,
            designation, quantite, prix,
            remiseField.getText().trim(), tvaField.getText().trim()));
        statusLabel.setText("");
    }

    @FXML
    public void removeLine() {
        LigneDraft selected = linesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lines.remove(selected);
        }
    }

    private void updateTotals() {
        double ht = 0, tva = 0;
        for (LigneDraft l : lines) {
            ht += l.lineHt();
            tva += l.lineTva();
        }
        totalsLabel.setText(Messages.get("form.totals",
            String.format("%.2f", ht), String.format("%.2f", tva),
            String.format("%.2f", ht + tva)));
    }

    private static boolean isPositiveNumber(String text) {
        try {
            return Double.parseDouble(text.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    public void save() {
        String error = validate();
        if (error != null) {
            statusLabel.setText(error);
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("codeBonLiv", codeBonLivField.getText().trim());
        payload.put("dateBonLiv", dateBonLivField.getText().trim());
        payload.put("client", clientCombo.getValue().getIdClient());
        payload.put("statutBonLiv", statutCombo.getValue());
        payload.put("observationBonLiv", observationField.getText().trim());
        List<Map<String, Object>> lignes = new ArrayList<>();
        for (LigneDraft l : lines) {
            lignes.add(l.toPayload());
        }
        payload.put("lignes", lignes);

        statusLabel.setText(Messages.get("form.saving"));
        new Thread(() -> {
            try {
                if (editing != null) {
                    api.put("/api/bonliv/" + editing.getIdBonLiv(), payload,
                        BonLiv.class);
                } else {
                    api.createBonLiv(payload);
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
        if (clientCombo.getValue() == null) {
            return Messages.get("form.chooseClient");
        }
        if (!dateBonLivField.getText().trim()
                .matches("\\d{4}-\\d{2}-\\d{2}")) {
            return Messages.get("form.dateFormat");
        }
        if (lines.isEmpty()) {
            return Messages.get("form.addLineFirst");
        }
        for (LigneDraft l : lines) {
            if (l.getDesignation() == null || l.getDesignation().isBlank()) {
                return Messages.get("form.lineNeedsDesignation");
            }
        }
        return null;
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
