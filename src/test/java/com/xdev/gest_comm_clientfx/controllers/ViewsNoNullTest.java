package com.xdev.gest_comm_clientfx.controllers;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * No screen may ship a literal "null": neither as a default {@code text}
 * nor as a prompt. The legacy report was exactly this — the Code Client
 * field showing "null" on a new client.
 */
class ViewsNoNullTest {

    private static final List<String> VIEWS = List.of(
        "HomeScreen.fxml", "ConfigScreen.fxml", "NouveauClient.fxml",
        "NouvelArticle.fxml", "NouveauDevis.fxml", "NouvelFacture.fxml",
        "NouveauBonLiv.fxml", "Assistant.fxml");

    private static String view(String name) throws Exception {
        String path = "/com/xdev/gest_comm_clientfx/views/" + name;
        try (InputStream in = ViewsNoNullTest.class.getResourceAsStream(path)) {
            assertNotNull(in, name + " must be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void noFormPrefillsAFieldWithTheLiteralNull() throws Exception {
        for (String name : VIEWS) {
            String fxml = view(name);
            assertFalse(fxml.contains("text=\"null\""),
                name + " prefills a field with the literal null");
            assertFalse(fxml.contains("promptText=\"null\""),
                name + " prompts with the literal null");
        }
    }

    @Test
    void documentCodeFieldsAnnounceAutoNumbering() throws Exception {
        String[][] expected = {
            {"NouveauDevis.fxml", "codeDevisField"},
            {"NouvelFacture.fxml", "codeFactureField"},
            {"NouveauBonLiv.fxml", "codeBonLivField"},
        };
        for (String[] pair : expected) {
            String fxml = view(pair[0]);
            assertTrue(fxml.contains("fx:id=\"" + pair[1] + "\""),
                pair[0] + " must keep " + pair[1]);
            assertTrue(fxml.contains("auto si vide"),
                pair[0] + " must tell the user the code can stay empty");
        }
    }
}
