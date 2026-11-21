package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.api.ApiException;
import com.xdev.gest_comm_clientfx.i18n.Messages;

import java.util.Locale;

/**
 * Phrasing for API failures — one place so the record screens cannot drift
 * apart. The API layer stays language-neutral; the intent ("delete", "save")
 * comes from the screen that knows what the user asked for, and the sentence
 * itself comes from the bundle in the language chosen on this workstation.
 */
public final class ApiMessages {

    private ApiMessages() {
    }

    /** Message for a refused action, e.g. "Delete failed: …". */
    public static String refused(String action, ApiException e) {
        return refused(Messages.locale(), action, e);
    }

    /** Same, in an explicit language (tests, capture tools). */
    public static String refused(Locale locale, String action, ApiException e) {
        String detail = e.getDetail();
        if (e.getStatusCode() == 409) {
            return Messages.get(locale, "error.conflict", action, detail);
        }
        if (e.getStatusCode() == 400) {
            return Messages.get(locale, "error.refused", detail);
        }
        if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
            return Messages.get(locale, "error.authRequired");
        }
        return Messages.get(locale, "error.generic", detail);
    }

    /**
     * Message for a failure that is not an API refusal (a bug, a missing
     * value). Never renders the literal "null": a {@code Throwable} with
     * no message falls back to its type.
     */
    public static String unexpected(Throwable e) {
        return unexpected(Messages.locale(), e);
    }

    /** Same, in an explicit language (tests, capture tools). */
    public static String unexpected(Locale locale, Throwable e) {
        String message = e == null ? null : e.getMessage();
        if (message == null || message.isBlank()) {
            String what = e == null
                ? Messages.get(locale, "error.unknown")
                : e.getClass().getSimpleName();
            return Messages.get(locale, "error.unexpected", what);
        }
        return Messages.get(locale, "error.unexpected", message);
    }

    /** Message for a failed read (list load, picker prefill). */
    public static String loadFailed(ApiException e) {
        return loadFailed(Messages.locale(), e);
    }

    /** Same, in an explicit language (tests, capture tools). */
    public static String loadFailed(Locale locale, ApiException e) {
        if (e.getStatusCode() == 401 || e.getStatusCode() == 403) {
            return Messages.get(locale, "error.authRequired");
        }
        return Messages.get(locale, "error.load", e.getDetail());
    }
}
