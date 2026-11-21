package com.xdev.gest_comm_clientfx.ui;

import java.util.prefs.Preferences;

/**
 * Workstation preferences: theme and language (specs/login-profile-settings
 * D4). They describe how this machine shows the app, so they live in Java
 * Preferences and never travel to the server.
 */
public final class AppPreferences {

    static final String SKIN_KEY = "skin";
    static final String LANGUAGE_KEY = "language";

    private final Preferences prefs;

    public AppPreferences() {
        this(Preferences.userNodeForPackage(AppPreferences.class));
    }

    /** Preferences-node injection, so the defaults can be tested. */
    AppPreferences(Preferences prefs) {
        this.prefs = prefs;
    }

    public static AppPreferences load() {
        return new AppPreferences();
    }

    public Theme.Skin skin() {
        return "light".equalsIgnoreCase(prefs.get(SKIN_KEY, "dark"))
            ? Theme.Skin.LIGHT : Theme.Skin.DARK;
    }

    public void setSkin(Theme.Skin skin) {
        prefs.put(SKIN_KEY, skin == Theme.Skin.LIGHT ? "light" : "dark");
    }

    /** {@code "fr"} (base bundle) or {@code "en"}. */
    public String language() {
        return "en".equalsIgnoreCase(prefs.get(LANGUAGE_KEY, "fr"))
            ? "en" : "fr";
    }

    public void setLanguage(String language) {
        prefs.put(LANGUAGE_KEY, "en".equalsIgnoreCase(language) ? "en" : "fr");
    }
}
