package com.xdev.gest_comm_clientfx.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The legacy defect this guards against: a screen printing the literal
 * words "null" where a value is missing.
 */
class DisplayTest {

    @Test
    void aMissingFieldValueBecomesEmptyNotTheWordNull() {
        assertEquals("", Display.text(null));
        assertEquals("42", Display.text(42));
        assertEquals("C001", Display.text("C001"));
    }

    @Test
    void aMissingFieldValueUsesTheGivenDefault() {
        assertEquals("0", Display.text(null, "0"));
        assertEquals("0", Display.text("   ", "0"));
        assertEquals("1.5", Display.text("1.5", "0"));
    }

    @Test
    void aMissingCellValueShowsADash() {
        assertEquals(Display.EMPTY_CELL, Display.cell(null));
        assertEquals(Display.EMPTY_CELL, Display.cell(""));
        assertEquals(Display.EMPTY_CELL, Display.cell("  "));
        assertEquals("FAC-0001", Display.cell("FAC-0001"));
        assertEquals("0", Display.cell(0));
    }

    @Test
    void noHelperEverProducesTheLiteralNull() {
        Object[] absent = {null, "", "   "};
        for (Object value : absent) {
            assertEquals(false, Display.text(value).equals("null"));
            assertEquals(false, Display.cell(value).equals("null"));
            assertEquals(false, Display.text(value, "0").equals("null"));
        }
    }
}
