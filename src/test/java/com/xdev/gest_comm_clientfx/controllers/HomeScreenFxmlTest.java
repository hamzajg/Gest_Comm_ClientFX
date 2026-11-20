package com.xdev.gest_comm_clientfx.controllers;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scenario: Home screen has no placeholder text and no empty filler column
 * (specs/delete-conflict-ux).
 *
 * The layout is checked as text so the suite stays toolkit-free: a JavaFX
 * {@code TableView} cannot be instantiated without an initialized toolkit,
 * which CI does not have. The column-resize half of the scenario is verified
 * on the captured screen in the wave evidence bundle (no filler column after
 * {@code TableView.CONSTRAINED_RESIZE_POLICY}).
 */
class HomeScreenFxmlTest {

    private static final String HOME =
        "/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml";

    private static String homeFxml() throws Exception {
        try (InputStream in =
                 HomeScreenFxmlTest.class.getResourceAsStream(HOME)) {
            assertNotNull(in, "HomeScreen.fxml must be on the classpath");
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void homeScreenHasNoLeftoverPlaceholderText() throws Exception {
        assertFalse(homeFxml().contains("Text Here"),
            "the header placeholder label must be gone");
        assertTrue(homeFxml().contains("fx:id=\"searchField\""),
            "the search field stays");
    }

    @Test
    void homeScreenDeclaresExactlyTheFiveListTables() throws Exception {
        Matcher m = Pattern.compile("<TableView\\b").matcher(homeFxml());
        int tables = 0;
        while (m.find()) {
            tables++;
        }
        assertEquals(5, tables,
            "one table per resource — an extra table would need columns");
    }
}
