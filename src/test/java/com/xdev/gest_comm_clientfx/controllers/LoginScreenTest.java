package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiException;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers "Sign in with valid credentials" / "Refuse wrong credentials"
 * (specs/login-profile-settings) without a JavaFX toolkit: the FXML is
 * checked as text and the refusal wording as a pure function. The rendered
 * login screen is verified by the wave's captures.
 */
class LoginScreenTest {

    private static final String LOGIN =
        "/com/xdev/gest_comm_clientfx/views/LoginView.fxml";

    private static String loginFxml() throws Exception {
        try (InputStream in =
                 LoginScreenTest.class.getResourceAsStream(LOGIN)) {
            assertNotNull(in, "LoginView.fxml must be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void loginViewWiresTheControllerAndItsFields() throws Exception {
        String fxml = loginFxml();
        assertTrue(fxml.contains(
            "fx:controller=\"com.xdev.gest_comm_clientfx.controllers.LoginCtrl\""),
            "the login view must be driven by LoginCtrl");
        for (String id : new String[] {
            "baseUrlField", "usernameField", "passwordField",
            "signInButton", "statusLabel"
        }) {
            assertTrue(fxml.contains("fx:id=\"" + id + "\""),
                "missing login field " + id);
        }
        assertTrue(fxml.contains("onAction=\"#signIn\""),
            "the login button must call signIn");
    }

    @Test
    void loginViewNeverPrefillsThePassword() throws Exception {
        String fxml = loginFxml();
        int field = fxml.indexOf("fx:id=\"passwordField\"");
        assertTrue(field > 0, "password field must exist");
        String tag = fxml.substring(field - 40, fxml.indexOf("/>", field));
        assertFalse(tag.contains("text=\""),
            "a prefilled password would put a secret on screen");
    }

    @Test
    void aRejectedLoginSaysWhatWentWrong() {
        ApiException wrongPassword = ApiException.fromResponse(
            "http://localhost:8000/api/token", 401,
            "{\"detail\":\"No active account found\"}");
        assertTrue(LoginCtrl.message(wrongPassword)
            .contains("incorrect"), "401 must read as wrong credentials");

        ApiException offline = new ApiException(
            "Cannot reach http://localhost:8000/api/token");
        assertTrue(LoginCtrl.message(offline).contains("serveur"),
            "a transport failure must point at the server address");

        ApiException refused = ApiException.fromResponse(
            "http://localhost:8000/api/token", 400,
            "{\"detail\":\"Bad request\"}");
        assertTrue(LoginCtrl.message(refused).contains("Bad request"),
            "other refusals keep the backend's own explanation");
    }
}
