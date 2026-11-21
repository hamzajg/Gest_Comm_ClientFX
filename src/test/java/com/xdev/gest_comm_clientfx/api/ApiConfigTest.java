package com.xdev.gest_comm_clientfx.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Covers "No stored password" (specs/login-profile-settings, US9/D3): the
 * password is used for the sign-in exchange and never reaches the OS
 * preferences store — not even when a previous release left one there.
 */
class ApiConfigTest {

    private Preferences node;

    @BeforeEach
    void freshNode() {
        node = Preferences.userRoot().node(
            "/com/xdev/gest_comm_clientfx/test/api-config-"
                + UUID.randomUUID());
    }

    @AfterEach
    void removeNode() throws Exception {
        node.removeNode();
    }

    private List<String> keys() throws Exception {
        return Arrays.asList(node.keys());
    }

    @Test
    void savingNeverWritesThePassword() throws Exception {
        ApiConfig config = ApiConfig.load(node);
        config.setBaseUrl("http://poste-01:8000");
        config.setUsername("operateur");
        config.setPassword("mot-de-passe-secret");
        config.setRefreshToken("refresh-1");
        config.save();

        assertFalse(keys().contains(ApiConfig.PASSWORD_KEY),
            "the password must not be persisted");

        ApiConfig reloaded = ApiConfig.load(node);
        assertEquals("http://poste-01:8000", reloaded.getBaseUrl());
        assertEquals("operateur", reloaded.getUsername());
        assertEquals("refresh-1", reloaded.getRefreshToken(),
            "the session must still survive a restart through the token");
        assertEquals("", reloaded.getPassword(),
            "a reloaded config carries no password");
    }

    @Test
    void aPasswordStoredByAnOlderReleaseIsErased() throws Exception {
        node.put(ApiConfig.PASSWORD_KEY, "ancien-secret");
        node.put("username", "operateur");

        ApiConfig config = ApiConfig.load(node);

        assertEquals("", config.getPassword());
        assertFalse(keys().contains(ApiConfig.PASSWORD_KEY),
            "loading the settings must clean up the legacy plaintext secret");
        assertEquals("operateur", config.getUsername());
    }
}
