package com.xdev.gest_comm_clientfx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Contextual actions for the active tab, shown on the right so acting on a
 * record never costs the user the tab they had open.
 */
public final class ActionDrawer {

    /** One button in the drawer. */
    public record Action(String label, Runnable handler) { }

    private final VBox root = new VBox(8);
    private final Label title = new Label();
    private final VBox actions = new VBox(6);
    private final Label hint = new Label();

    public ActionDrawer() {
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #555;");
        root.setPadding(new Insets(10));
        root.setPrefWidth(210);
        root.setMinWidth(190);
        root.setMaxWidth(240);
        root.setStyle("-fx-border-color: transparent transparent transparent"
            + " #d8d8d8; -fx-background-color: #fafafa;");

        Label caption = new Label("ACTIONS");
        caption.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        HBox header = new HBox(6, caption, spacer(),
            closeButton());
        header.setAlignment(Pos.CENTER_LEFT);
        actions.setFillWidth(true);
        root.getChildren().addAll(header, title, new Separator(), actions, hint);
    }

    public Node getRoot() {
        return root;
    }

    /**
     * Point the drawer at the active tab. An empty list means "nothing to do
     * here", which is still better than showing the previous tab's buttons.
     */
    public void show(String heading, List<Action> entries) {
        title.setText(heading == null || heading.isBlank() ? "—" : heading);
        actions.getChildren().clear();
        for (Action entry : entries) {
            Button b = new Button(entry.label());
            b.setOnAction(e -> entry.handler().run());
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            actions.getChildren().add(b);
        }
        hint.setText(entries.isEmpty()
            ? "Aucune action pour cet onglet." : "");
    }

    public void setHint(String text) {
        hint.setText(text);
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    public void toggle() {
        boolean show = !root.isVisible();
        root.setVisible(show);
        root.setManaged(show);
    }

    public boolean isShowing() {
        return root.isVisible();
    }

    private Button closeButton() {
        Button close = new Button("›");
        close.setStyle("-fx-background-color: transparent; -fx-padding: 0 4 0 4;");
        close.setOnAction(e -> hide());
        return close;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
}
