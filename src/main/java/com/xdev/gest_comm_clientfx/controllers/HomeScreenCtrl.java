package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.app.ConfigScreen;
import com.xdev.gest_comm_clientfx.i18n.Messages;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.BonLiv;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.Facture;
import com.xdev.gest_comm_clientfx.models.UserProfile;
import com.xdev.gest_comm_clientfx.ui.ActionDrawer;
import com.xdev.gest_comm_clientfx.ui.TabWorkspace;
import com.xdev.gest_comm_clientfx.ui.Views;
import com.xdev.gest_comm_clientfx.ui.WorkspacePolicy;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Home shell. Panels open as tabs in the workspace, never as windows: the
 * menu and the drawer both route into {@link TabWorkspace#open}, and the
 * drawer lists what can be done on the tab that is currently open.
 */
public class HomeScreenCtrl {

    @FXML private TabPane tabs;
    @FXML private VBox drawerHost;
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
    @FXML private Label searchScope;
    @FXML private VBox clientsPanel;
    @FXML private VBox articlesPanel;
    @FXML private VBox devisPanel;
    @FXML private VBox facturesPanel;
    @FXML private VBox bonlivsPanel;
    @FXML private VBox panelStore;

    private final ApiClient api = new ApiClient(ApiConfig.load());
    private final ActionDrawer drawer = new ActionDrawer();
    /** The signed-in account; null until /api/me answers. */
    private volatile UserProfile sessionProfile;
    private TabWorkspace workspace;
    private String searchTerm = "";
    private int formSequence = 0;
    private Runnable onSignedOut;
    private Runnable onLanguageChanged;

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

        workspace = new TabWorkspace(tabs);
        drawerHost.getChildren().add(drawer.getRoot());
        tabs.getSelectionModel().selectedItemProperty()
            .addListener((o, old, tab) -> updateDrawer(tab));
        tabs.getTabs().addListener(
            (javafx.collections.ListChangeListener<Tab>) change -> {
                if (tabs.getTabs().isEmpty()) {
                    // The workspace without any tab would leave the user
                    // with no surface at all: fall back to Bienvenue.
                    showWelcome();
                }
            });
        showWelcome();
        loadSessionProfile();
    }

    /** Role for the drawer: read from /api/me, never from the client (D1). */
    private void loadSessionProfile() {
        new Thread(() -> {
            try {
                UserProfile profile = api.profile();
                Platform.runLater(() -> {
                    sessionProfile = profile;
                    updateDrawer(workspace.active());
                });
            } catch (ApiException ignored) {
                // The gate handles a dead session; the drawer then offers
                // only the non-administrative actions.
            }
        }).start();
    }

    private boolean isAdmin() {
        return sessionProfile != null && sessionProfile.isAdmin();
    }

    // --- tabs --------------------------------------------------------

    /** Only Bienvenue is open at startup. */
    @FXML
    public void showWelcome() {
        Tab tab = workspace.open(WorkspacePolicy.WELCOME,
            WorkspacePolicy.title(WorkspacePolicy.WELCOME), welcomePanel());
        updateDrawer(tab);
    }

    private Node welcomePanel() {
        Label title = new Label(Messages.get("welcome.title"));
        title.getStyleClass().add("welcome-title");
        Label subtitle = new Label(Messages.get("welcome.subtitle"));
        subtitle.getStyleClass().add("welcome-subtitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(720);

        HBox shortcuts = new HBox(8,
            shortcut(Messages.get("menu.clients"), this::showClients),
            shortcut(Messages.get("menu.articles"), this::showArticles),
            shortcut(Messages.get("menu.devis"), this::showDevis),
            shortcut(Messages.get("menu.factures"), this::showFactures),
            shortcut(Messages.get("menu.bonLivs"), this::showBonLivs));
        VBox box = new VBox(12, title, subtitle, shortcuts);
        box.getStyleClass().add("welcome-panel");
        box.setPadding(new Insets(24));
        return box;
    }

    private Button shortcut(String label, Runnable action) {
        Button b = new Button(label);
        b.getStyleClass().add("chip");
        b.setOnAction(e -> action.run());
        return b;
    }

    private void openList(String key, Node panel, Runnable refresh) {
        workspace.open(key, WorkspacePolicy.title(key), panel);
        updateDrawer(workspace.active());
        refresh.run();
    }

    @FXML
    public void showClients() {
        openList(WorkspacePolicy.CLIENTS, clientsPanel, this::refreshClients);
    }

    @FXML
    public void showArticles() {
        openList(WorkspacePolicy.ARTICLES, articlesPanel,
            this::refreshArticles);
    }

    @FXML
    public void showDevis() {
        openList(WorkspacePolicy.DEVIS, devisPanel, this::refreshDevis);
    }

    @FXML
    public void showFactures() {
        openList(WorkspacePolicy.FACTURES, facturesPanel,
            this::refreshFactures);
    }

    @FXML
    public void showBonLivs() {
        openList(WorkspacePolicy.BONLIVS, bonlivsPanel, this::refreshBonLivs);
    }

    @FXML
    public void openConfig() {
        try {
            Node root = new ConfigScreen().getRootView();
            Tab tab = workspace.open(WorkspacePolicy.CONFIG,
                WorkspacePolicy.title(WorkspacePolicy.CONFIG), root);
            updateDrawer(tab);
        } catch (IOException e) {
            clientStatus.setText(Messages.get("error.openConfig"));
        }
    }

    /** The signed-in account, its role and the way out (US3). */
    @FXML
    public void showProfile() {
        try {
            FXMLLoader loader = Views.loader(getClass(), "ProfileView.fxml");
            Parent root = loader.load();
            ProfileCtrl ctrl = loader.getController();
            ctrl.setOnSignedOut(this::requestSignOut);
            ctrl.setOnLanguageChanged(this::requestLanguageChange);
            Tab tab = workspace.open(WorkspacePolicy.PROFILE,
                WorkspacePolicy.title(WorkspacePolicy.PROFILE), root);
            updateDrawer(tab);
        } catch (IOException e) {
            clientStatus.setText(Messages.get("error.openProfile"));
        }
    }

    /** Loads a form FXML into a tab instead of its own window. */
    private <C> void openForm(String fxml, String title, Class<C> ctrlType,
                              Runnable onSaved) {
        openForm(fxml, title, ctrlType, onSaved, c -> { });
    }

    private <C> void openForm(String fxml, String title, Class<C> ctrlType,
                              Runnable onSaved, Consumer<C> configure) {
        try {
            URL url = getClass().getResource(fxml);
            FXMLLoader loader = new FXMLLoader(url);
            loader.setResources(Messages.resourceBundle());
            Parent form = loader.load();
            C ctrl = ctrlType.cast(loader.getController());
            // A form taller than the window (devis with many lines, or a
            // 1024x640 minimum window) must scroll, never clip its actions.
            ScrollPane scroller = new ScrollPane(form);
            scroller.setFitToWidth(true);
            scroller.getStyleClass().add("tab-scroll");
            Tab tab = workspace.open(
                WorkspacePolicy.formKey(title, ++formSequence),
                title, scroller);
            if (ctrl instanceof ApiDialog d) {
                d.setApi(api);
                d.setOnSaved(onSaved);
                // Fermer / Enregistrer close this tab, not a window.
                d.setOnClosed(() -> workspace.close(tab));
            }
            configure.accept(ctrl);
            updateDrawer(tab);
        } catch (IOException e) {
            clientStatus.setText(Messages.get("error.openForm"));
        }
    }

    // --- drawer ------------------------------------------------------

    private void updateDrawer(Tab tab) {
        if (tab == null) {
            drawer.show("—", List.of());
            return;
        }
        String key = workspace.keyOf(tab);
        String name = Display.text(tab.getText()).replace("★ ", "")
            .replace(" — ", "");
        drawer.show(name,
            ActionDrawer.visible(actionsFor(key, tab), isAdmin()));
        searchScope.setText(WorkspacePolicy.isList(key)
            ? Messages.get("search.scope", name) : "");
    }

    private List<ActionDrawer.Action> actionsFor(String key, Tab tab) {
        if (key == null) {
            return tabActions(tab, List.of());
        }
        List<ActionDrawer.Action> base = switch (key) {
            case WorkspacePolicy.WELCOME -> List.of(
                action(Messages.get("menu.profile"), this::showProfile),
                action(Messages.get("menu.clients"), this::showClients),
                action(Messages.get("menu.articles"), this::showArticles),
                action(Messages.get("menu.devis"), this::showDevis),
                action(Messages.get("menu.factures"), this::showFactures),
                action(Messages.get("menu.bonLivs"), this::showBonLivs));
            case WorkspacePolicy.CLIENTS -> List.of(
                action(Messages.get("action.refresh"), this::refreshClients),
                action(Messages.get("action.newClient"), this::showNewClient),
                adminAction(Messages.get("action.edit"), this::editSelectedClient),
                adminAction(Messages.get("action.delete"), this::deleteSelectedClient));
            case WorkspacePolicy.ARTICLES -> List.of(
                action(Messages.get("action.refresh"), this::refreshArticles),
                action(Messages.get("action.newArticle"), this::showNewArticle),
                adminAction(Messages.get("action.edit"), this::editSelectedArticle),
                adminAction(Messages.get("action.delete"), this::deleteSelectedArticle));
            case WorkspacePolicy.DEVIS -> List.of(
                action(Messages.get("action.refresh"), this::refreshDevis),
                action(Messages.get("action.newDevis"), this::showNewDevis),
                adminAction(Messages.get("action.edit"), this::editSelectedDevis),
                adminAction(Messages.get("action.validate"), this::validerDevis),
                adminAction(Messages.get("action.refuse"), this::refuserDevis),
                adminAction(Messages.get("action.convert"), this::convertSelectedDevis),
                adminAction(Messages.get("action.delete"), this::deleteSelectedDevis));
            case WorkspacePolicy.FACTURES -> List.of(
                action(Messages.get("action.refresh"), this::refreshFactures),
                action(Messages.get("action.newFacture"), this::showNewFacture),
                adminAction(Messages.get("action.edit"), this::editSelectedFacture),
                adminAction(Messages.get("action.validate"), this::validerFacture),
                adminAction(Messages.get("action.markPaid"), this::payerFacture),
                adminAction(Messages.get("action.cancel"), this::annulerFacture),
                adminAction(Messages.get("action.delete"), this::deleteSelectedFacture));
            case WorkspacePolicy.BONLIVS -> List.of(
                action(Messages.get("action.refresh"), this::refreshBonLivs),
                action(Messages.get("action.newBonLiv"), this::showNewBonLiv),
                adminAction(Messages.get("action.edit"), this::editSelectedBonLiv),
                adminAction(Messages.get("action.validate"), this::validerBonLiv),
                adminAction(Messages.get("action.markDelivered"), this::livrerBonLiv),
                adminAction(Messages.get("action.cancel"), this::annulerBonLiv),
                adminAction(Messages.get("action.delete"), this::deleteSelectedBonLiv));
            case WorkspacePolicy.CONFIG -> List.of();
            default -> List.of();
        };
        return tabActions(tab, base);
    }

    /** Pin and close are offered on every tab, so the drawer replaces the ×/★. */
    private List<ActionDrawer.Action> tabActions(
            Tab tab, List<ActionDrawer.Action> base) {
        List<ActionDrawer.Action> all = new ArrayList<>(base);
        all.add(action(workspace.isPinned(tab)
                ? Messages.get("drawer.unpin") : Messages.get("drawer.pin"),
            () -> {
                workspace.togglePin(tab);
                updateDrawer(tab);
            }));
        all.add(action(Messages.get("drawer.closeTab"),
            () -> workspace.close(tab)));
        return all;
    }

    private static ActionDrawer.Action action(String label, Runnable r) {
        return new ActionDrawer.Action(label, r);
    }

    /** Shown to administrators only; presentation gating (US8). */
    private static ActionDrawer.Action adminAction(String label, Runnable r) {
        return new ActionDrawer.Action(label, r, true);
    }

    @FXML
    public void toggleDrawer() {
        drawer.toggle();
    }

    // --- search ------------------------------------------------------

    private void applySearch() {
        searchTerm = searchField.getText().trim();
        String key = workspace.activeKey();
        if (WorkspacePolicy.CLIENTS.equals(key)) {
            refreshClients();
        } else if (WorkspacePolicy.ARTICLES.equals(key)) {
            refreshArticles();
        } else if (WorkspacePolicy.DEVIS.equals(key)) {
            refreshDevis();
        } else if (WorkspacePolicy.FACTURES.equals(key)) {
            refreshFactures();
        } else if (WorkspacePolicy.BONLIVS.equals(key)) {
            refreshBonLivs();
        }
    }

    // --- tables ------------------------------------------------------

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

    // --- list loading ------------------------------------------------

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

    @FXML
    public void refreshDevis() {
        load("devis", devisStatus,
            () -> FXCollections.observableArrayList(api.listDevis(searchTerm)),
            devisTable::setItems);
    }

    @FXML
    public void refreshFactures() {
        load("facture", factureStatus,
            () -> FXCollections.observableArrayList(api.listFactures(searchTerm)),
            factureTable::setItems);
    }

    @FXML
    public void refreshBonLivs() {
        load("bon de livraison", bonlivStatus,
            () -> FXCollections.observableArrayList(api.listBonLivs(searchTerm)),
            bonlivTable::setItems);
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

    // --- documents ---------------------------------------------------

    @FXML
    public void showNewDevis() {
        openForm("/com/xdev/gest_comm_clientfx/views/NouveauDevis.fxml",
            "Nouveau devis", NouveauDevisCtrl.class, this::refreshDevis);
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
    public void showNewFacture() {
        openForm("/com/xdev/gest_comm_clientfx/views/NouvelFacture.fxml",
            "Nouvelle facture", NouvelFactureCtrl.class,
            this::refreshFactures);
    }

    @FXML
    public void showNewBonLiv() {
        openForm("/com/xdev/gest_comm_clientfx/views/NouveauBonLiv.fxml",
            "Nouveau bon de livraison", NouveauBonLivCtrl.class,
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
        openForm("/com/xdev/gest_comm_clientfx/views/NouveauBonLiv.fxml",
            "Modifier le bon de livraison", NouveauBonLivCtrl.class,
            this::refreshBonLivs, c -> c.setEditing(b));
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

    // --- clients / articles ------------------------------------------

    @FXML
    public void showNewClient() {
        openForm("/com/xdev/gest_comm_clientfx/views/NouveauClient.fxml",
            "Nouveau client", NouveauClientCtrl.class, this::refreshClients);
    }

    @FXML
    public void showNewArticle() {
        openForm("/com/xdev/gest_comm_clientfx/views/NouvelArticle.fxml",
            "Nouvel article", NouvelArticleCtrl.class, this::refreshArticles);
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
        openForm("/com/xdev/gest_comm_clientfx/views/NouveauClient.fxml",
            "Modifier le client", NouveauClientCtrl.class,
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
        openForm("/com/xdev/gest_comm_clientfx/views/NouvelArticle.fxml",
            "Modifier l'article", NouvelArticleCtrl.class,
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
        openForm("/com/xdev/gest_comm_clientfx/views/NouveauDevis.fxml",
            "Modifier le devis", NouveauDevisCtrl.class,
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
        openForm("/com/xdev/gest_comm_clientfx/views/NouvelFacture.fxml",
            "Modifier la facture", NouvelFactureCtrl.class,
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

    @FXML
    public void showAssistant() {
        openForm("/com/xdev/gest_comm_clientfx/views/Assistant.fxml",
            "Assistant", AssistantCtrl.class, () -> { });
    }

    /** Sign-out: revoke the session, then return to the login gate (US3). */
    @FXML
    public void logout() {
        new Thread(() -> {
            api.logout();
            Platform.runLater(this::requestSignOut);
        }).start();
    }

    /** Called by the shell that owns the stage once the session is revoked. */
    public void setOnSignedOut(Runnable onSignedOut) {
        this.onSignedOut = onSignedOut;
    }

    /** Called when the profile changes the language; rebuilds the scene. */
    public void setOnLanguageChanged(Runnable onLanguageChanged) {
        this.onLanguageChanged = onLanguageChanged;
    }

    private void requestSignOut() {
        if (onSignedOut != null) {
            onSignedOut.run();
        }
    }

    private void requestLanguageChange() {
        if (onLanguageChanged != null) {
            onLanguageChanged.run();
        }
    }

    /** Kept for callers that still hold a Stage (tests, tools). */
    static void closeStage(Node node) {
        if (node.getScene() != null
                && node.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }
}
