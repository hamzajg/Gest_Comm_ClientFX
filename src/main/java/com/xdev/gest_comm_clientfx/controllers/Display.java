package com.xdev.gest_comm_clientfx.controllers;

/**
 * What a screen shows for a value that may be absent.
 *
 * Carried over from a legacy client defect ("le champ Code Client contient
 * « null »"): Java string concatenation turns a missing value into the
 * literal text {@code null}, and an empty table cell looks like a bug.
 * Screens format through this class instead:
 * <ul>
 *   <li>{@link #text(Object)} — form fields: absent becomes empty, never
 *       "null".</li>
 *   <li>{@link #cell(Object)} — grids and sentences: absent becomes "—" so
 *       the row reads as "no value" rather than as a rendering fault.</li>
 * </ul>
 */
public final class Display {

    /** Shown where a value is expected but the record has none. */
    public static final String EMPTY_CELL = "—";

    private Display() {
    }

    /** Text for an input field: {@code null} becomes {@code ""}. */
    public static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /** Text for an input field with a default when absent or blank. */
    public static String text(Object value, String fallback) {
        String text = text(value).trim();
        return text.isEmpty() ? fallback : text;
    }

    /** Text for a table cell or sentence: {@code null}/blank becomes "—". */
    public static String cell(Object value) {
        String text = text(value).trim();
        return text.isEmpty() ? EMPTY_CELL : text;
    }
}
