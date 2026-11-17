package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.LigneDevis;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NouveauDevisCtrl implements ApiDialog {

    /** Editable draft row for the lines table — strings keep the FXML
     *  simple; the backend accepts numeric strings for Decimal/Float. */
    public static class LigneDraft {
        private final Long articleId;
        private final String designation;
        private final String quantite;
        private final String prix;
        private final String remise;
        private final String tva;

        LigneDraft(Long articleId, String designation, String quantite,
                   String prix, String remise, String tva) {
            this.articleId = articleId;
            this.designation = designation;
            this.quantite = quantite;
            this.prix = prix;
            this.remise = remise;
            this.tva = tva;
        }

        public Long getArticleId() { return articleId; }
        public String getDesignation() { return designation; }
        public String getQuantite() { return quantite; }
        public String getPrix() { return prix; }
        public String getRemise() { return remise; }
        public String getTva() { return tva; }

        public Map<String, Object> toPayload() {
            Map<String, Object> m = new HashMap<>();
            if (articleId != null) {
                m.put("article", articleId);
            }
            m.put("designation", designation);
            m.put("quantite", quantite);
            m.put("prixUnitaire", prix);
            m.put("remise", remise.isEmpty() ? "0" : remise);
            m.put("tva", tva.isEmpty() ? "0" : tva);
            return m;
        }

        double lineHt() {
            try {
                double q = Double.parseDouble(quantite);
                double p = Double.parseDouble(prix);
                double r = remise.isEmpty() ? 0 : Double.parseDouble(remise);
                return q * p * (1 - r / 100.0);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        double lineTva() {
            try {
                double t = tva.isEmpty() ? 0 : Double.parseDouble(tva);
                return lineHt() * t / 100.0;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    @FXML private TextField codeDevisField;
    @FXML private ComboBox<Client> clientCombo;
    @FXML private TextField dateDevisField;
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
    private Devis editing;
    private Long pendingClientId;
    private final ObservableList<LigneDraft> lines =
        FXCollections.observableArrayList();
    private List<Client> clients = new ArrayList<>();
    private List<Article> articles = new ArrayList<>();

    /** Prefill the form for editing an existing devis (PUT on save;
     *  the backend replaces lignes when the payload includes them). */
    public void setEditing(Devis devis) {
        this.editing = devis;
        this.pendingClientId = devis.getClient();
        codeDevisField.setText(devis.getCodeDevis());
        dateDevisField.setText(textOr(devis.getDateDevis(), ""));
        observationField.setText(textOr(devis.getObservationDevis(), ""));
        if (devis.getStatutDevis() != null) {
            statutCombo.getSelectionModel().select(devis.getStatutDevis());
        }
        lines.clear();
        if (devis.getLignes() != null) {
            for (LigneDevis l : devis.getLignes()) {
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
        linesTable.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldV, newV) -> updateTotals());
        lines.addListener((javafx.collections.ListChangeListener<LigneDraft>)
            c -> updateTotals());

        dateDevisField.setText(LocalDate.now().toString());
        statutCombo.getItems().addAll("brouillon", "valide", "refuse");
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
            statusLabel.setText("Choisissez un article ou saisissez une désignation.");
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
        payload.put("codeDevis", codeDevisField.getText().trim());
        payload.put("dateDevis", dateDevisField.getText().trim());
        payload.put("client", clientCombo.getValue().getIdClient());
        payload.put("statutDevis", statutCombo.getValue());
        payload.put("observationDevis", observationField.getText().trim());
        List<Map<String, Object>> lignes = new ArrayList<>();
        for (LigneDraft l : lines) {
            lignes.add(l.toPayload());
        }
        payload.put("lignes", lignes);

        statusLabel.setText("Enregistrement…");
        new Thread(() -> {
            try {
                if (editing != null) {
                    api.put("/api/devis/" + editing.getIdDevis(), payload,
                        Devis.class);
                } else {
                    api.createDevis(payload);
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
        if (codeDevisField.getText().isBlank()) {
            return "Le code devis est obligatoire.";
        }
        if (clientCombo.getValue() == null) {
            return "Choisissez un client.";
        }
        if (!dateDevisField.getText().trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
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
