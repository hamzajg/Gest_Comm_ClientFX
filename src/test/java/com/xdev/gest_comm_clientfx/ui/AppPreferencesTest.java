package com.xdev.gest_comm_clientfx.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers "Theme survives a restart" and "Language switches the shell"
 * (specs/login-profile-settings US5/US6, D4): the choices are local, survive
 * a reload, and an unknown stored value falls back to the defaults.
 */
class AppPreferencesTest {

    private Preferences node;

    @BeforeEach
    void freshNode() {
        node = Preferences.userRoot().node(
            "/com/xdev/gest_comm_clientfx/test/prefs-" + UUID.randomUUID());
    }

    @AfterEach
    void removeNode() throws Exception {
        node.removeNode();
    }

    @Test
    void defaultsAreTheDarkSkinAndFrench() {
        AppPreferences prefs = new AppPreferences(node);
        assertEquals(Theme.Skin.DARK, prefs.skin());
        assertEquals("fr", prefs.language());
    }

    @Test
    void choicesSurviveAReload() {
        AppPreferences prefs = new AppPreferences(node);
        prefs.setSkin(Theme.Skin.LIGHT);
        prefs.setLanguage("en");

        AppPreferences reloaded = new AppPreferences(node);
        assertEquals(Theme.Skin.LIGHT, reloaded.skin());
        assertEquals("en", reloaded.language());
    }

    @Test
    void unknownStoredValuesFallBackToTheDefaults() {
        node.put(AppPreferences.SKIN_KEY, "neon");
        node.put(AppPreferences.LANGUAGE_KEY, "de");

        AppPreferences prefs = new AppPreferences(node);
        assertEquals(Theme.Skin.DARK, prefs.skin());
        assertEquals("fr", prefs.language());
    }
}
