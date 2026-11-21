package com.xdev.gest_comm_clientfx.api;

import com.xdev.gest_comm_clientfx.controllers.ApiMessages;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Scenario: A blocked delete explains why in French /
 * API errors expose status and backend detail
 * (specs/delete-conflict-ux).
 */
class ApiExceptionTest {

    @Test
    void keepsStatusAndBackendDetailFromAConflict() {
        ApiException e = ApiException.fromResponse(
            "http://localhost:8000/api/client/3", 409,
            "{\"detail\":\"This client cannot be deleted: still referenced "
                + "by 1 devis.\",\"protected\":[{\"model\":\"devis\","
                + "\"label\":\"devis\",\"count\":1}]}");

        assertEquals(409, e.getStatusCode());
        assertEquals("This client cannot be deleted: still referenced by "
            + "1 devis.", e.getDetail());
        assertEquals("Suppression impossible : This client cannot be "
            + "deleted: still referenced by 1 devis.",
            ApiMessages.refused(Locale.FRENCH, "Suppression", e));
    }

    @Test
    void surfacesTheFirstFieldErrorForAValidationAnswer() {
        ApiException e = ApiException.fromResponse(
            "http://localhost:8000/api/assistant/ask", 400,
            "{\"question\":[\"Ce champ est obligatoire.\"]}");

        assertEquals(400, e.getStatusCode());
        assertEquals("question : Ce champ est obligatoire.", e.getDetail());
        assertEquals("Action refusée : question : Ce champ est obligatoire.",
            ApiMessages.refused(Locale.FRENCH, "Enregistrement", e));
    }

    @Test
    void fallsBackToTheRawBodyForANonJsonAnswer() {
        ApiException e = ApiException.fromResponse(
            "http://localhost:8000/api/client/3", 502, "<html>Bad gateway");

        assertEquals(502, e.getStatusCode());
        assertTrue(e.getDetail().contains("Bad gateway"));
        assertTrue(e.getMessage().startsWith("HTTP 502 from"));
    }

    @Test
    void anUnexpectedFailureNeverRendersTheLiteralNull() {
        assertEquals("Erreur inattendue : IllegalStateException",
            ApiMessages.unexpected(Locale.FRENCH, new IllegalStateException()));
        assertEquals("Erreur inattendue : boom",
            ApiMessages.unexpected(Locale.FRENCH, new RuntimeException("boom")));
        assertEquals("Erreur inattendue : inconnue",
            ApiMessages.unexpected(Locale.FRENCH, null));
    }

    @Test
    void transportFailuresCarryNoStatus() {
        ApiException e = new ApiException("Cannot reach http://x — down");

        assertEquals(0, e.getStatusCode());
        assertEquals("Cannot reach http://x — down", e.getDetail());
    }
}
