package com.xdev.gest_comm_clientfx.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Which panels exist, what they are called and whether opening one twice
 * creates a second tab. Kept free of JavaFX so the rules can be tested
 * without a toolkit.
 */
public final class WorkspacePolicy {

    public static final String WELCOME = "welcome";
    public static final String CLIENTS = "clients";
    public static final String ARTICLES = "articles";
    public static final String DEVIS = "devis";
    public static final String FACTURES = "factures";
    public static final String BONLIVS = "bonlivs";
    public static final String CONFIG = "config";

    /** Panels that exist at most once: reopening focuses the existing tab. */
    private static final Set<String> SINGLETONS = Set.of(
        WELCOME, CLIENTS, ARTICLES, DEVIS, FACTURES, BONLIVS, CONFIG);

    /** Panels whose table the toolbar search filters. */
    private static final Set<String> LISTS = Set.of(
        CLIENTS, ARTICLES, DEVIS, FACTURES, BONLIVS);

    private static final Map<String, String> TITLES = Map.of(
        WELCOME, "Bienvenue",
        CLIENTS, "Clients",
        ARTICLES, "Articles",
        DEVIS, "Devis",
        FACTURES, "Factures",
        BONLIVS, "Bons de livraison",
        CONFIG, "Paramètres");

    private WorkspacePolicy() { }

    public static boolean isSingleInstance(String key) {
        return SINGLETONS.contains(key);
    }

    public static boolean isList(String key) {
        return LISTS.contains(key);
    }

    public static String title(String key) {
        return TITLES.getOrDefault(key, key);
    }

    /** A form tab is never reused: two clients can be created side by side. */
    public static String formKey(String form, int sequence) {
        return "form:" + form + ":" + sequence;
    }

    public static boolean isForm(String key) {
        return key != null && key.startsWith("form:");
    }

    public static List<String> lists() {
        return List.copyOf(LISTS);
    }
}
