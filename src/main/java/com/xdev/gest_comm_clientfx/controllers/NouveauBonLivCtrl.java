package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
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
        codeBonLivField.setText(bonliv.getCodeBonLiv());
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

        statusLabel.setText("Chargement des clients et articles…");
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
            Platform.runLater(() -> statusLabel.setText(
                "Erreur de chargement : " + e.getMessage()));
        }
    }

    private void prefillFromArticle(Article article) {
        if (article == null) {
            return;
        }
        designationField.setText(article.getDesc_article());
        prixField.setText(textOr(article.getPrix_vente_unitaire(), "0"));
        remiseField.setText(textOr(article.getRemise_vente(), "0"));
        tvaField.setText(textOr(article.getTva_vente(), "0"));
    }

    private static String textOr(Object value, String fallback) {
        return value != null ? String.valueOf(value) : fallback;
    }

    @FXML
    public void addLine() {
        String designation = designationField.getText().trim();
        String quantite = quantiteField.getText().trim();
        String prix = prixField.getText().trim();
        if (designation.isEmpty() && articleCombo.getValue() == null) {
            statusLabel.setText(
                "Choisissez un article ou saisissez une désignation.");
            return;
        }
        if (designation.isEmpty()) {
            designation = articleCombo.getValue().getDesc_article();
        }
        if (!isPositiveNumber(quantite) || !isPositiveNumber(prix)) {
            statusLabel.setText("Quantité et prix doivent être des nombres > 0.");
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
        totalsLabel.setText(String.format(
            "Total HT : %.2f — TVA : %.2f — TTC : %.2f",
            ht, tva, ht + tva));
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

        statusLabel.setText("Enregistrement…");
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
                Platform.runLater(() ->
                    statusLabel.setText("Erreur : " + e.getMessage()));
            }
        }).start();
    }

    private String validate() {
        if (codeBonLivField.getText().isBlank()) {
            return "Le code bon de livraison est obligatoire.";
        }
        if (clientCombo.getValue() == null) {
            return "Choisissez un client.";
        }
        if (!dateBonLivField.getText().trim()
                .matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "La date doit être au format AAAA-MM-JJ.";
        }
        if (lines.isEmpty()) {
            return "Ajoutez au moins une ligne.";
        }
        for (LigneDraft l : lines) {
            if (l.getDesignation() == null || l.getDesignation().isBlank()) {
                return "Chaque ligne doit avoir une désignation.";
            }
        }
        return null;
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
