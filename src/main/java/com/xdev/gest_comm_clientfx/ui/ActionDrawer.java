package com.xdev.gest_comm_clientfx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Contextual actions for the active tab, shown on the right so acting on a
 * record never costs the user the tab they had open.
 *
 * The pane is always there: it expands to its full width and collapses to a
 * narrow rail that keeps the toggle in reach, so the workspace never jumps
 * because a panel disappeared.
 */
public final class ActionDrawer {

    /** One button in the drawer. */
    public record Action(String label, Runnable handler) { }

    private static final double OPEN_WIDTH = 210;
    private static final double RAIL_WIDTH = 30;

    private final VBox root = new VBox(8);
    private final VBox body = new VBox(8);
    private final Label title = new Label();
    private final VBox actions = new VBox(6);
    private final Label hint = new Label();
    private final Button collapse = new Button();
    private final Label caption = new Label("ACTIONS");
    private final Region headerSpacer = spacer();
    private final HBox header;
    private boolean collapsed;

    public ActionDrawer() {
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: #555;");
        root.setPadding(new Insets(10));
        root.setStyle("-fx-border-color: transparent transparent transparent"
            + " #d8d8d8; -fx-background-color: #fafafa;");
        root.setMinHeight(Region.USE_PREF_SIZE);

        caption.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        collapse.setStyle("-fx-background-color: transparent;"
            + " -fx-padding: 0 4 0 4; -fx-font-size: 13px;");
        collapse.setOnAction(e -> toggle());

        header = new HBox(6, caption, headerSpacer, collapse);
        header.setAlignment(Pos.CENTER_LEFT);

        actions.setFillWidth(true);
        body.getChildren().addAll(title, new Separator(), actions, hint);

        root.getChildren().addAll(header, body);
        setCollapsed(false);
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

    /** Collapse to the rail, or expand back to the full action list. */
    public void toggle() {
        setCollapsed(!collapsed);
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        body.setVisible(!collapsed);
        body.setManaged(!collapsed);
        double width = collapsed ? RAIL_WIDTH : OPEN_WIDTH;
        root.setPrefWidth(width);
        root.setMinWidth(width);
        root.setMaxWidth(width);
        root.setPadding(collapsed ? new Insets(10, 2, 10, 2)
            : new Insets(10));
        caption.setVisible(!collapsed);
        caption.setManaged(!collapsed);
        headerSpacer.setVisible(!collapsed);
        headerSpacer.setManaged(!collapsed);
        header.setAlignment(collapsed ? Pos.CENTER : Pos.CENTER_LEFT);
        collapse.setText(collapsed ? "‹" : "›");
        collapse.setTooltip(new Tooltip(collapsed
            ? "Déplier le panneau d'actions"
            : "Replier le panneau d'actions"));
        // The rail keeps the toggle vertically available.
        root.setAlignment(collapsed ? Pos.TOP_CENTER : Pos.TOP_LEFT);
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    /** Kept for callers that used the old hide/show wording. */
    public boolean isShowing() {
        return !collapsed;
    }

    private static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }
}
