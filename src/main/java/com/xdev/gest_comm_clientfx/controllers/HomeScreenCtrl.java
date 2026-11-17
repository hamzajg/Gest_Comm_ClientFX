package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiClient;
import com.xdev.gest_comm_clientfx.api.ApiConfig;
import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.app.ConfigScreen;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.Client;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.function.Supplier;

public class HomeScreenCtrl {

    @FXML private TabPane tabs;
    @FXML private Tab clientsTab;
    @FXML private Tab articlesTab;
    @FXML private TableView<Client> clientTable;
    @FXML private TableView<Article> articleTable;
    @FXML private Label clientStatus;
    @FXML private Label articleStatus;

    private final ApiClient api = new ApiClient(ApiConfig.load());

    @FXML
    public void initialize() {
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
    }

    private static <T> TableColumn<T, Object> column(
            String title, String property, double width) {
        TableColumn<T, Object> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
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
    public void refreshClients() {
        load("clients", clientStatus,
            () -> FXCollections.observableArrayList(api.listClients()),
            clientTable::setItems);
    }

    @FXML
    public void refreshArticles() {
        load("articles", articleStatus,
            () -> FXCollections.observableArrayList(api.listArticles()),
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
                Platform.runLater(() -> status.setText("Erreur : " + e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> status.setText(
                    "Erreur : " + e.getClass().getSimpleName() + " " + e.getMessage()));
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

    private <C> void showDialog(String fxml, String title, double w,
                                double h, Class<C> ctrlType,
                                Runnable onSaved) {
        try {
            URL url = getClass().getResource(fxml);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            C ctrl = ctrlType.cast(loader.getController());
            if (ctrl instanceof NouveauClientCtrl c) {
                c.setApi(api);
                c.setOnSaved(onSaved);
            } else if (ctrl instanceof NouvelArticleCtrl c) {
                c.setApi(api);
                c.setOnSaved(onSaved);
            }
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.show();
        } catch (IOException e) {
            clientStatus.setText("Erreur : impossible d'ouvrir le formulaire.");
        }
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
