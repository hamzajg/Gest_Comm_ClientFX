package com.xdev.gest_comm_clientfx.controllers;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers "Profile shows identity and role" and "Sign out returns to login"
 * (specs/login-profile-settings, US3) as view wiring, plus the US9 rule that
 * the connection settings no longer carry credentials. The rendered screens
 * are verified by the wave's captures.
 */
class ProfileScreenTest {

    private static String view(String name) throws Exception {
        String path = "/com/xdev/gest_comm_clientfx/views/" + name;
        try (InputStream in = ProfileScreenTest.class.getResourceAsStream(path)) {
            assertNotNull(in, name + " must be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void profileViewShowsIdentityRoleAndTheWayOut() throws Exception {
        String fxml = view("ProfileView.fxml");
        assertTrue(fxml.contains(
            "fx:controller=\"com.xdev.gest_comm_clientfx.controllers.ProfileCtrl\""),
            "the profile view must be driven by ProfileCtrl");
        for (String id : new String[] {
            "nameLabel", "usernameLabel", "emailLabel", "roleLabel",
            "signOutButton", "statusLabel"
        }) {
            assertTrue(fxml.contains("fx:id=\"" + id + "\""),
                "missing profile field " + id);
        }
        assertTrue(fxml.contains("onAction=\"#signOut\""),
            "the profile must offer signing out");
        assertTrue(fxml.contains("styleClass=\"role-badge\""),
            "the role is a badge, not a bare label");
    }

    @Test
    void connectionSettingsKeepOnlyTheServerAddress() throws Exception {
        String fxml = view("ConfigScreen.fxml");
        assertTrue(fxml.contains("fx:id=\"baseUrlField\""),
            "the server URL stays editable");
        assertFalse(fxml.contains("passwordField"),
            "US9: the settings screen must not ask for a password");
        assertFalse(fxml.contains("PasswordField"),
            "US9: no password control belongs in the settings screen");
        assertFalse(fxml.contains("usernameField"),
            "the account is entered on the login gate, not in settings");
    }

    @Test
    void applicationMenuOpensTheProfile() throws Exception {
        String fxml = view("HomeScreen.fxml");
        assertTrue(fxml.contains("onAction=\"#showProfile\""),
            "the shell must expose the profile tab");
        assertTrue(fxml.contains("text=\"Mon profil\""),
            "the menu entry must be readable");
    }
}
