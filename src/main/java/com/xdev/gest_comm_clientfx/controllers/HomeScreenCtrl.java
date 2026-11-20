package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.app.ConfigScreen;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.BonLiv;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.Facture;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HomeScreenCtrl {

    @FXML private TabPane tabs;
    @FXML private Tab clientsTab;
    @FXML private Tab articlesTab;
    @FXML private Tab devisTab;
    @FXML private Tab factureTab;
    @FXML private Tab bonlivTab;
    @FXML private TableView<Client> clientTable;
    @FXML private TableView<Article> articleTable;
    @FXML private TableView<Devis> devisTable;
    @FXML private TableView<Facture> factureTable;
    @FXML private TableView<BonLiv> bonlivTable;
    @FXML private Label clientStatus;
    @FXML private Label articleStatus;
    @FXML private Label devisStatus;
    @FXML private Label factureStatus;
    @FXML private Label bonlivStatus;
    @FXML private TextField searchField;

    private final ApiClient api = new ApiClient(ApiConfig.load());
    private String searchTerm = "";

    @FXML
    public void initialize() {
        for (TableView<?> table : new TableView<?>[]{clientTable,
                articleTable, devisTable, factureTable, bonlivTable}) {
            applyColumnPolicy(table);
        }
        clientTable.getColumns().addAll(
            column("Code", "codeClient", 100),
            column("Nom", "nomPrenomClient", 220),
            column("Mat. fiscale", "matFiscalClient", 140),
            column("Tél. 1", "numTelClient1", 110),
            column("Tél. 2", "numTelClient2", 110));
        articleTable.getColumns().addAll(
            column("Référence", "ref_article", 120),
            column("Désignation", "desc_article", 260),
            column("Prix vente", "prix_vente_unitaire", 110));
        devisTable.getColumns().addAll(
            column("Code", "codeDevis", 90),
            column("Date", "dateDevis", 100),
            column("Client", "codeClient", 90),
            column("Nom", "nomPrenomClient", 200),
            column("Statut", "statutDevis", 90),
            column("Total TTC", "totalTTC", 100));
        factureTable.getColumns().addAll(
            column("Code", "codeFacture", 90),
            column("Date", "dateFacture", 100),
            column("Client", "codeClient", 90),
            column("Nom", "nomPrenomClient", 200),
            column("Devis", "codeDevis", 90),
            column("Statut", "statutFacture", 90),
            column("Total TTC", "totalTTC", 100));
        bonlivTable.getColumns().addAll(
            column("Code", "codeBonLiv", 90),
            column("Date", "dateBonLiv", 100),
            column("Client", "codeClient", 90),
            column("Nom", "nomPrenomClient", 200),
            column("Statut", "statutBonLiv", 90),
            column("Total TTC", "totalTTC", 100));
        searchField.setOnAction(e -> applySearch());
    }

    /** Re-run the list load for whichever tab is selected, with the
     *  toolbar search term as ?search=. */
    private void applySearch() {
        searchTerm = searchField.getText().trim();
        Tab selected = tabs.getSelectionModel().getSelectedItem();
        if (selected == clientsTab) {
            refreshClients();
        } else if (selected == articlesTab) {
            refreshArticles();
        } else if (selected == devisTab) {
            refreshDevis();
        } else if (selected == factureTab) {
            refreshFactures();
        } else if (selected == bonlivTab) {
            refreshBonLivs();
        }
    }

    /**
     * Constrained resize: the tables declare exactly the columns their data
     * warrants, so the default unconstrained policy must not append an
     * empty filler column at the right edge.
     */
    static void applyColumnPolicy(TableView<?> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private static <T> TableColumn<T, Object> column(
            String title, String property, double width) {
        TableColumn<T, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        // A missing field is shown as "—": an empty cell reads as a
        // rendering bug (the legacy "null" report), a dash reads as "this
        // record has no value here".
        col.setCellFactory(c -> new TableCell<T, Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : Display.cell(item));
            }
        });
        col.setPrefWidth(width);
        return col;
    }

    @FXML
    public void showClients() {
        tabs.getSelectionModel().select(clientsTab);
        refreshClients();
    }

    @FXML
    public void showArticles() {
        tabs.getSelectionModel().select(articlesTab);
        refreshArticles();
    }

    @FXML
    public void showDevis() {
        tabs.getSelectionModel().select(devisTab);
        refreshDevis();
    }

    @FXML
    public void refreshDevis() {
        load("devis", devisStatus,
            () -> FXCollections.observableArrayList(api.listDevis(searchTerm)),
            devisTable::setItems);
    }

    @FXML
    public void showNewDevis() {
        showDialog("/com/xdev/gest_comm_clientfx/views/NouveauDevis.fxml",
            "Nouveau devis", 720, 560, NouveauDevisCtrl.class,
            this::refreshDevis);
    }

    @FXML
    public void convertSelectedDevis() {
        Devis devis = devisTable.getSelectionModel().getSelectedItem();
        if (devis == null) {
            devisStatus.setText("Sélectionnez un devis à convertir.");
            return;
        }
        devisStatus.setText("Conversion en cours…");
        new Thread(() -> {
            try {
                Facture f = api.convertDevis(devis.getIdDevis(),
                    Map.of());
                Platform.runLater(() -> {
                    devisStatus.setText("Devis converti → facture "
                        + Display.cell(f.getCodeFacture()));
                    refreshDevis();
                    refreshFactures();
                });
            } catch (ApiException e) {
                Platform.runLater(() -> devisStatus.setText(
                    ApiMessages.refused("Conversion", e)));
            }
        }).start();
    }

    @FXML
    public void showFactures() {
        tabs.getSelectionModel().select(factureTab);
        refreshFactures();
    }

    @FXML
    public void refreshFactures() {
        load("facture", factureStatus,
            () -> FXCollections.observableArrayList(api.listFactures(searchTerm)),
            factureTable::setItems);
    }

    @FXML
    public void showNewFacture() {
        showDialog("/com/xdev/gest_comm_clientfx/views/NouvelFacture.fxml",
            "Nouvelle facture", 720, 580, NouvelFactureCtrl.class,
            this::refreshFactures);
    }

    @FXML
    public void showBonLivs() {
        tabs.getSelectionModel().select(bonlivTab);
        refreshBonLivs();
    }

    @FXML
    public void refreshBonLivs() {
        load("bon de livraison", bonlivStatus,
            () -> FXCollections.observableArrayList(api.listBonLivs(searchTerm)),
            bonlivTable::setItems);
    }

    @FXML
    public void showNewBonLiv() {
        showDialog("/com/xdev/gest_comm_clientfx/views/NouveauBonLiv.fxml",
            "Nouveau bon de livraison", 720, 560, NouveauBonLivCtrl.class,
            this::refreshBonLivs);
    }

    private void setBonLivStatut(String statut) {
        BonLiv b = bonlivTable.getSelectionModel().getSelectedItem();
        if (b == null) {
            bonlivStatus.setText("Sélectionnez un bon de livraison.");
            return;
        }
        mutate(bonlivStatus, "Statut du bon de livraison mis à jour.",
            "Mise à jour",
            () -> api.patch("/api/bonliv/" + b.getIdBonLiv(),
                Map.of("statutBonLiv", statut)),
            this::refreshBonLivs);
    }

    @FXML public void validerBonLiv() { setBonLivStatut("valide"); }
    @FXML public void livrerBonLiv() { setBonLivStatut("livre"); }
    @FXML public void annulerBonLiv() { setBonLivStatut("annule"); }

    @FXML
    public void editSelectedBonLiv() {
        BonLiv b = bonlivTable.getSelectionModel().getSelectedItem();
        if (b == null) {
            bonlivStatus.setText("Sélectionnez un bon de livraison.");
            return;
        }
        showDialog("/com/xdev/gest_comm_clientfx/views/NouveauBonLiv.fxml",
            "Modifier le bon de livraison", 720, 560,
            NouveauBonLivCtrl.class, this::refreshBonLivs,
            c -> c.setEditing(b));
    }

    @FXML
    public void deleteSelectedBonLiv() {
        BonLiv b = bonlivTable.getSelectionModel().getSelectedItem();
        if (b == null) {
            bonlivStatus.setText("Sélectionnez un bon de livraison.");
            return;
        }
        if (!confirmDelete("le bon de livraison "
                + Display.cell(b.getCodeBonLiv()))) {
            return;
        }
        mutate(bonlivStatus, "Bon de livraison supprimé.", "Suppression",
            () -> api.delete("/api/bonliv/" + b.getIdBonLiv()),
            this::refreshBonLivs);
    }

    @FXML
    public void refreshClients() {
        load("clients", clientStatus,
            () -> FXCollections.observableArrayList(api.listClients(searchTerm)),
            clientTable::setItems);
    }

    @FXML
    public void refreshArticles() {
        load("articles", articleStatus,
            () -> FXCollections.observableArrayList(api.listArticles(searchTerm)),
            articleTable::setItems);
    }

    private <T> void load(String label, Label status,
                          Supplier<javafx.collections.ObservableList<T>> fetch,
                          java.util.function.Consumer<
                              javafx.collections.ObservableList<T>> apply) {
        status.setText("Chargement…");
        new Thread(() -> {
            try {
                var data = fetch.get();
                Platform.runLater(() -> {
                    apply.accept(data);
                    status.setText(data.size() + " " + label + " chargé(s).");
                });
            } catch (ApiException e) {
                Platform.runLater(() ->
                    status.setText(ApiMessages.loadFailed(e)));
            } catch (Exception e) {
                Platform.runLater(() ->
                    status.setText(ApiMessages.unexpected(e)));
            }
        }).start();
    }

    @FXML
    public void showNewClient() {
        showDialog("/com/xdev/gest_comm_clientfx/views/NouveauClient.fxml",
            "Nouveau client", 480, 460, NouveauClientCtrl.class,
            this::refreshClients);
    }

    @FXML
    public void showNewArticle() {
        showDialog("/com/xdev/gest_comm_clientfx/views/NouvelArticle.fxml",
            "Nouvel article", 480, 520, NouvelArticleCtrl.class,
            this::refreshArticles);
    }

    // --- row actions: edit / delete / statut -------------------------

    private void mutate(Label status, String okMessage, String action,
                        Runnable call, Runnable refresh) {
        status.setText("Enregistrement…");
        new Thread(() -> {
            try {
                call.run();
                Platform.runLater(() -> {
                    refresh.run();
                    status.setText(okMessage);
                });
            } catch (ApiException e) {
                Platform.runLater(() ->
                    status.setText(ApiMessages.refused(action, e)));
            }
        }).start();
    }

    private boolean confirmDelete(String what) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Supprimer " + what + " ?");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    @FXML
    public void editSelectedClient() {
        Client c = clientTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            clientStatus.setText("Sélectionnez un client.");
            return;
        }
        showDialog("/com/xdev/gest_comm_clientfx/views/NouveauClient.fxml",
            "Modifier le client", 480, 460, NouveauClientCtrl.class,
            this::refreshClients, d -> d.setEditing(c));
    }

    @FXML
    public void deleteSelectedClient() {
        Client c = clientTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            clientStatus.setText("Sélectionnez un client.");
            return;
        }
        if (!confirmDelete("le client " + Display.cell(c.getCodeClient()))) {
            return;
        }
        mutate(clientStatus, "Client supprimé.", "Suppression",
            () -> api.delete("/api/client/" + c.getIdClient()),
            this::refreshClients);
    }

    @FXML
    public void editSelectedArticle() {
        Article a = articleTable.getSelectionModel().getSelectedItem();
        if (a == null) {
            articleStatus.setText("Sélectionnez un article.");
            return;
        }
        showDialog("/com/xdev/gest_comm_clientfx/views/NouvelArticle.fxml",
            "Modifier l'article", 480, 520, NouvelArticleCtrl.class,
            this::refreshArticles, d -> d.setEditing(a));
    }

    @FXML
    public void deleteSelectedArticle() {
        Article a = articleTable.getSelectionModel().getSelectedItem();
        if (a == null) {
            articleStatus.setText("Sélectionnez un article.");
            return;
        }
        if (!confirmDelete("l'article "
                + Display.cell(a.getRef_article()))) {
            return;
        }
        mutate(articleStatus, "Article supprimé.", "Suppression",
            () -> api.delete("/api/article/" + a.getId_article()),
            this::refreshArticles);
    }

    private void setDevisStatut(String statut) {
        Devis d = devisTable.getSelectionModel().getSelectedItem();
        if (d == null) {
            devisStatus.setText("Sélectionnez un devis.");
            return;
        }
        mutate(devisStatus, "Statut du devis mis à jour.", "Mise à jour",
            () -> api.patch("/api/devis/" + d.getIdDevis(),
                Map.of("statutDevis", statut)),
            this::refreshDevis);
    }

    @FXML public void validerDevis() { setDevisStatut("valide"); }
    @FXML public void refuserDevis() { setDevisStatut("refuse"); }

    @FXML
    public void editSelectedDevis() {
        Devis d = devisTable.getSelectionModel().getSelectedItem();
        if (d == null) {
            devisStatus.setText("Sélectionnez un devis.");
            return;
        }
        showDialog("/com/xdev/gest_comm_clientfx/views/NouveauDevis.fxml",
            "Modifier le devis", 720, 560, NouveauDevisCtrl.class,
            this::refreshDevis, c -> c.setEditing(d));
    }

    @FXML
    public void deleteSelectedDevis() {
        Devis d = devisTable.getSelectionModel().getSelectedItem();
        if (d == null) {
            devisStatus.setText("Sélectionnez un devis.");
            return;
        }
        if (!confirmDelete("le devis " + Display.cell(d.getCodeDevis()))) {
            return;
        }
        mutate(devisStatus, "Devis supprimé.", "Suppression",
            () -> api.delete("/api/devis/" + d.getIdDevis()),
            this::refreshDevis);
    }

    private void setFactureStatut(String statut) {
        Facture f = factureTable.getSelectionModel().getSelectedItem();
        if (f == null) {
            factureStatus.setText("Sélectionnez une facture.");
            return;
        }
        mutate(factureStatus, "Statut de la facture mis à jour.",
            "Mise à jour",
            () -> api.patch("/api/facture/" + f.getIdFacture(),
                Map.of("statutFacture", statut)),
            this::refreshFactures);
    }

    @FXML public void validerFacture() { setFactureStatut("validee"); }
    @FXML public void payerFacture() { setFactureStatut("payee"); }
    @FXML public void annulerFacture() { setFactureStatut("annulee"); }

    @FXML
    public void editSelectedFacture() {
        Facture f = factureTable.getSelectionModel().getSelectedItem();
        if (f == null) {
            factureStatus.setText("Sélectionnez une facture.");
            return;
        }
        showDialog("/com/xdev/gest_comm_clientfx/views/NouvelFacture.fxml",
            "Modifier la facture", 720, 580, NouvelFactureCtrl.class,
            this::refreshFactures, c -> c.setEditing(f));
    }

    @FXML
    public void deleteSelectedFacture() {
        Facture f = factureTable.getSelectionModel().getSelectedItem();
        if (f == null) {
            factureStatus.setText("Sélectionnez une facture.");
            return;
        }
        if (!confirmDelete("la facture " + Display.cell(f.getCodeFacture()))) {
            return;
        }
        mutate(factureStatus, "Facture supprimée.", "Suppression",
            () -> api.delete("/api/facture/" + f.getIdFacture()),
            this::refreshFactures);
    }

    private <C> void showDialog(String fxml, String title, double w,
                                double h, Class<C> ctrlType,
                                Runnable onSaved) {
        showDialog(fxml, title, w, h, ctrlType, onSaved, c -> { });
    }

    private <C> void showDialog(String fxml, String title, double w,
                                double h, Class<C> ctrlType,
                                Runnable onSaved, Consumer<C> configure) {
        try {
            URL url = getClass().getResource(fxml);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            C ctrl = ctrlType.cast(loader.getController());
            if (ctrl instanceof ApiDialog d) {
                d.setApi(api);
                d.setOnSaved(onSaved);
            }
            configure.accept(ctrl);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.show();
        } catch (IOException e) {
            clientStatus.setText("Erreur : impossible d'ouvrir le formulaire.");
        }
    }

    @FXML
    public void showAssistant() {
        showDialog("/com/xdev/gest_comm_clientfx/views/Assistant.fxml",
            "Assistant", 640, 520, AssistantCtrl.class, () -> { });
    }

    @FXML
    public void logout() {
        new Thread(() -> {
            api.logout();
            Platform.runLater(() -> clientStatus.setText(
                "Session révoquée — la prochaine action reconnectera "
                + "automatiquement."));
        }).start();
    }

    @FXML
    public void openConfig() {
        try {
            Stage stage = new Stage();
            stage.setTitle("Paramètres de l'application");
            stage.setScene(new Scene(new ConfigScreen().getRootView(), 560, 320));
            stage.show();
        } catch (IOException e) {
            clientStatus.setText("Erreur : impossible d'ouvrir la configuration.");
        }
    }
}
