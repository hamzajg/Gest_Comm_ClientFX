package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiException;

/**
 * French phrasing for API failures — one place so five controllers cannot
 * drift apart. The API layer stays language-neutral; the intent ("supprimer",
 * "enregistrer") comes from the screen that knows what the user asked for.
 */
public final class ApiMessages {

    private ApiMessages() {
    }

    /** Message for a refused action, e.g. "Suppression impossible : …". */
    public static String refused(String action, ApiException e) {
        String detail = e.getDetail();
        if (e.getStatusCode() == 409) {
            return action + " impossible : " + detail;
        }
        if (e.getStatusCode() == 400) {
            return "Action refusée : " + detail;
        }
        if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
            return "Authentification requise : vérifiez les paramètres "
                + "de l'application.";
        }
        return "Erreur : " + detail;
    }

    /**
     * Message for a failure that is not an API refusal (a bug, a missing
     * value). Never renders the literal "null": a {@code Throwable} with
     * no message falls back to its type.
     */
    public static String unexpected(Throwable e) {
        String message = e == null ? null : e.getMessage();
        if (message == null || message.isBlank()) {
            return "Erreur inattendue : "
                + (e == null ? "inconnue" : e.getClass().getSimpleName());
        }
        return "Erreur inattendue : " + message;
    }

    /** Message for a failed read (list load, picker prefill). */
    public static String loadFailed(ApiException e) {
        if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
            return "Authentification requise : vérifiez les paramètres "
                + "de l'application.";
        }
        return "Erreur de chargement : " + e.getDetail();
    }
}
