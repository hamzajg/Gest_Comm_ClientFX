package com.xdev.gest_comm_clientfx.i18n;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers "Language switches the shell" (specs/login-profile-settings US6):
 * both bundles carry exactly the same keys, every {@code %key} an FXML view
 * asks for exists, and English really is English. A missing key otherwise
 * shows up as a broken layout at runtime, not as a failed build.
 */
class MessagesBundleTest {

    private static final List<String> VIEWS = List.of(
        "HomeScreen.fxml", "LoginView.fxml", "ProfileView.fxml",
        "ConfigScreen.fxml");

    private static final Pattern KEY = Pattern.compile("%([A-Za-z0-9_.]+)");

    private static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle(Messages.BUNDLE, locale);
    }

    @Test
    void frenchAndEnglishDeclareTheSameKeys() {
        Set<String> fr = new TreeSet<>(bundle(Locale.FRENCH).keySet());
        Set<String> en = new TreeSet<>(bundle(Locale.ENGLISH).keySet());
        assertEquals(fr, en, "the two bundles must stay in step");
    }

    @Test
    void everyFxmlKeyExistsInBothLanguages() throws Exception {
        ResourceBundle fr = bundle(Locale.FRENCH);
        ResourceBundle en = bundle(Locale.ENGLISH);
        int checked = 0;
        for (String view : VIEWS) {
            String fxml = read(view);
            Matcher m = KEY.matcher(fxml);
            while (m.find()) {
                String key = m.group(1);
                assertTrue(fr.containsKey(key),
                    view + " asks for missing French key " + key);
                assertTrue(en.containsKey(key),
                    view + " asks for missing English key " + key);
                checked++;
            }
        }
        assertTrue(checked >= 15,
            "expected the shell, login and profile views to use the bundles");
    }

    @Test
    void theChosenLanguageChangesTheText() {
        assertEquals("Mon profil", Messages.get(Locale.FRENCH, "profile.title"));
        assertEquals("My profile", Messages.get(Locale.ENGLISH, "profile.title"));
        // MessageFormat arguments are substituted in both bundles.
        assertEquals("Connecté : Ada",
            Messages.get(Locale.FRENCH, "login.connected", "Ada"));
        assertEquals("Signed in: Ada",
            Messages.get(Locale.ENGLISH, "login.connected", "Ada"));
    }

    private static String read(String view) throws Exception {
        String path = "/com/xdev/gest_comm_clientfx/views/" + view;
        try (InputStream in = MessagesBundleTest.class.getResourceAsStream(path)) {
            assertNotNull(in, view + " must be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
