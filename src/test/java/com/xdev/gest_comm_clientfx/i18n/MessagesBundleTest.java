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
 * Covers "Language switches the shell" (specs/login-profile-settings US6) and
 * "the record tables and forms follow the chosen language" (US7): both bundles
 * carry exactly the same keys, every {@code %key} an FXML view asks for
 * exists, every visible label goes through the bundle, and English really is
 * English. A missing key otherwise shows up as a broken layout at runtime,
 * not as a failed build.
 */
class MessagesBundleTest {

    /** Every FXML view ships translated, the record forms and assistant
     *  included (US7). */
    private static final List<String> VIEWS = List.of(
        "HomeScreen.fxml", "LoginView.fxml", "ProfileView.fxml",
        "ConfigScreen.fxml", "NouveauClient.fxml", "NouvelArticle.fxml",
        "NouveauDevis.fxml", "NouvelFacture.fxml", "NouveauBonLiv.fxml",
        "Assistant.fxml");

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
        assertTrue(checked >= 40,
            "expected every view, record forms included, to use the bundles");
    }

    /**
     * No view may bake a French (or English) literal into a label or a
     * prompt: a literal is invisible to the language switch and is exactly
     * the gap US7 closes.
     */
    @Test
    void everyVisibleTextComesFromTheBundle() throws Exception {
        Pattern attribute = Pattern.compile(
            "(?:promptT|t)ext=\"([^\"]*)\"");
        for (String view : VIEWS) {
            Matcher m = attribute.matcher(read(view));
            while (m.find()) {
                String value = m.group(1);
                // An empty label and a URL example read the same in every
                // language; anything else must come from the bundle.
                boolean neutral = value.isEmpty()
                    || value.startsWith("%")
                    || value.startsWith("http://")
                    || value.startsWith("https://");
                assertTrue(neutral,
                    view + " hard-codes the text \"" + value + "\"");
            }
        }
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

    @Test
    void theRecordTablesAndFormsFollowTheLanguage() {
        assertEquals("Mat. fiscale",
            Messages.get(Locale.FRENCH, "table.taxId"));
        assertEquals("Tax ID", Messages.get(Locale.ENGLISH, "table.taxId"));

        assertEquals("Total TTC",
            Messages.get(Locale.FRENCH, "table.totalTtc"));
        assertEquals("Total incl. tax",
            Messages.get(Locale.ENGLISH, "table.totalTtc"));

        assertEquals("Nouveau devis",
            Messages.get(Locale.FRENCH, "form.devis.new"));
        assertEquals("New quote",
            Messages.get(Locale.ENGLISH, "form.devis.new"));

        assertEquals("auto si vide (ex. DEV-0001)",
            Messages.get(Locale.FRENCH, "field.codeDevisHint"));
        assertEquals("auto if empty (e.g. DEV-0001)",
            Messages.get(Locale.ENGLISH, "field.codeDevisHint"));

        assertEquals("3 clients chargés.",
            Messages.get(Locale.FRENCH, "record.loaded.clients", 3));
        assertEquals("3 clients loaded.",
            Messages.get(Locale.ENGLISH, "record.loaded.clients", 3));

        assertEquals("Total HT : 10.00 — TVA : 2.00 — TTC : 12.00",
            Messages.get(Locale.FRENCH, "form.totals",
                "10.00", "2.00", "12.00"));
        assertEquals("Total excl. tax: 10.00 — VAT: 2.00 — "
                + "Total incl. tax: 12.00",
            Messages.get(Locale.ENGLISH, "form.totals",
                "10.00", "2.00", "12.00"));
    }

    private static String read(String view) throws Exception {
        String path = "/com/xdev/gest_comm_clientfx/views/" + view;
        try (InputStream in = MessagesBundleTest.class.getResourceAsStream(path)) {
            assertNotNull(in, view + " must be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
