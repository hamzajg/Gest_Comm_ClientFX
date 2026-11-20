package com.xdev.gest_comm_clientfx.ui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Turns the home shell's TabPane into a workspace: panels open as tabs,
 * every tab carries a close button, a tab can be pinned (pinned tabs cannot
 * be closed), and singleton panels reuse their existing tab.
 */
public final class TabWorkspace {

    private static final String PIN = "★ ";

    private final TabPane pane;
    private final Map<String, Tab> byKey = new LinkedHashMap<>();
    private final Map<Tab, String> keyOf = new IdentityHashMap<>();
    private final Set<Tab> pinned =
        Collections.newSetFromMap(new IdentityHashMap<>());

    public TabWorkspace(TabPane pane) {
        this.pane = pane;
    }

    public TabPane pane() {
        return pane;
    }

    public Tab open(String key, String title, Node content) {
        if (WorkspacePolicy.isSingleInstance(key)) {
            Tab existing = byKey.get(key);
            if (existing != null) {
                pane.getSelectionModel().select(existing);
                return existing;
            }
        }
        Tab tab = new Tab(title, content);
        tab.setClosable(true);
        tab.setOnClosed(e -> forget(tab));
        keyOf.put(tab, key);
        if (WorkspacePolicy.isSingleInstance(key)) {
            byKey.put(key, tab);
        }
        pane.getTabs().add(tab);
        pane.getSelectionModel().select(tab);
        return tab;
    }

    public void close(Tab tab) {
        pane.getTabs().remove(tab);
        forget(tab);
    }

    /** Pinning keeps a tab open: it loses its close button. */
    public void togglePin(Tab tab) {
        if (tab == null) {
            return;
        }
        if (pinned.contains(tab)) {
            pinned.remove(tab);
            tab.setText(unpin(tab.getText()));
            tab.setClosable(true);
        } else {
            pinned.add(tab);
            tab.setText(PIN + unpin(tab.getText()));
            tab.setClosable(false);
        }
    }

    public boolean isPinned(Tab tab) {
        return tab != null && pinned.contains(tab);
    }

    public String keyOf(Tab tab) {
        return tab == null ? null : keyOf.get(tab);
    }

    public Tab active() {
        return pane.getSelectionModel().getSelectedItem();
    }

    public String activeKey() {
        return keyOf(active());
    }

    public boolean isEmpty() {
        return pane.getTabs().isEmpty();
    }

    private void forget(Tab tab) {
        keyOf.remove(tab);
        pinned.remove(tab);
        byKey.values().removeIf(t -> t == tab);
    }

    private static String unpin(String text) {
        return text != null && text.startsWith(PIN)
            ? text.substring(PIN.length()) : text;
    }
}
