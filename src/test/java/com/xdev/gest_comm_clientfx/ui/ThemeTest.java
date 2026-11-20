package com.xdev.gest_comm_clientfx.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The theme is data, so the failure mode is a stylesheet that quietly
 * disappears or loses its palette/component rules. Guard the sheet here
 * instead of opening a window in CI.
 */
class ThemeTest {

    /** Every token the component rules may read; each skin must define all. */
    private static final String[] PALETTE = {
        "-ink-900", "-surface-850", "-surface-800", "-surface-750",
        "-surface-700", "-steel-600", "-steel-500", "-text-100",
        "-text-300", "-text-400", "-amber", "-amber-dim", "-amber-glow",
        "-ok", "-danger", "-shadow", "-row-rule", "-press",
        "-on-accent", "-primary-hover"
    };

    private static String sheet() throws IOException {
        URL url = Theme.class.getResource(Theme.STYLESHEET);
        assertNotNull(url, "industrial.css must be on the classpath at " + Theme.STYLESHEET);
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** The base palette block, up to the light block. */
    private static String darkBlock(String css) {
        int start = css.indexOf(".root {");
        int lightStart = css.indexOf(".root.light {");
        assertTrue(start >= 0 && lightStart > start,
            "expected the base .root block before .root.light");
        return css.substring(start, lightStart);
    }

    /** The light palette block. */
    private static String lightBlock(String css) {
        int start = css.indexOf(".root.light {");
        assertTrue(start >= 0, "the light skin must override the .root token block");
        return css.substring(start, css.indexOf('}', start) + 1);
    }

    private static int count(String haystack, String needle) {
        int n = 0;
        for (int i = haystack.indexOf(needle); i >= 0;
                i = haystack.indexOf(needle, i + 1)) {
            n++;
        }
        return n;
    }

    @Test
    void stylesheetShipsOnTheClasspath() throws IOException {
        assertTrue(sheet().length() > 2_000, "the sheet should carry the full component rules");
    }

    @Test
    void paletteIsDeclaredOncePerSkin() throws IOException {
        String css = sheet();
        String dark = darkBlock(css);
        String light = lightBlock(css);
        for (String token : PALETTE) {
            assertEquals(1, count(dark, token + ":"),
                token + " must be declared once in the dark block");
            assertEquals(1, count(light, token + ":"),
                token + " must be declared once in the light block");
        }
    }

    @Test
    void noColourIsHardcodedOutsideTheTokenBlocks() throws IOException {
        String css = sheet();
        String rules = css.replace(darkBlock(css), "")
            .replace(lightBlock(css), "")
            .replaceAll("(?s)/\\*.*?\\*/", "");
        // A raw colour in a component rule cannot be re-skinned: every colour
        // has to resolve through a token the light block can override.
        assertFalse(rules.contains("#"), "hard-coded colour outside a token block");
        assertFalse(rules.contains("rgba("), "hard-coded colour outside a token block");
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
            ".table-view:focused", ".form-panel", ".form-title",
            ".form-panel .label", ".tab-scroll", ".transcript",
            ".assistant-hint"
        }) {
            assertTrue(css.contains(selector), "missing rule for " + selector);
        }
    }
}
