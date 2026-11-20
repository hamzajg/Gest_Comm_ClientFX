package com.xdev.gest_comm_clientfx.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The theme is data, so the failure mode is a stylesheet that quietly
 * disappears or loses its palette/component rules. Guard the sheet here
 * instead of opening a window in CI.
 */
class ThemeTest {

    private static String sheet() throws IOException {
        URL url = Theme.class.getResource(Theme.STYLESHEET);
        assertNotNull(url, "industrial.css must be on the classpath at " + Theme.STYLESHEET);
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void stylesheetShipsOnTheClasspath() throws IOException {
        assertTrue(sheet().length() > 2_000, "the sheet should carry the full component rules");
    }

    @Test
    void paletteIsDefinedOnceAsLookedUpColours() throws IOException {
        String css = sheet();
        for (String token : new String[] {
            "-ink-900:", "-surface-850:", "-surface-800:", "-surface-750:",
            "-steel-600:", "-text-100:", "-text-400:", "-amber:"
        }) {
            assertTrue(css.contains(token), "missing palette token " + token);
        }
        // Tokens are declared, not consumed, exactly once on .root.
        assertTrue(css.indexOf("-amber:") == css.lastIndexOf("-amber:"),
            "-amber must be declared once and only consumed elsewhere");
    }

    @Test
    void stateRulesOutSpecifyTheBaseRules() throws IOException {
        String css = sheet();
        // Regression: a short `.tab:hover` loses to the full base selector, so
        // the hover state silently never painted (found by tools/ThemeStates).
        assertTrue(css.contains(".headers-region > .tab:hover"),
            "tab hover must repeat the full selector or it never applies");
        assertTrue(css.contains(".button.drawer-action:pressed"),
            "drawer pressed needs its own rule to read against the drawer surface");
        assertTrue(css.contains(".button.drawer-action:focused"),
            "drawer buttons need a visible focus ring");
    }

    @Test
    void componentRulesCoverTheShellTablesAndDrawer() throws IOException {
        String css = sheet();
        for (String selector : new String[] {
            ".menu-bar", ".search-field", ".tab-pane", ".table-view",
            ".table-row-cell:selected", ".action-drawer", ".drawer-action",
            ".drawer-toggle", ".button.chip", ".welcome-title",
            ".table-view:focused", ".form-panel", ".form-title"
        }) {
            assertTrue(css.contains(selector), "missing rule for " + selector);
        }
    }
}
