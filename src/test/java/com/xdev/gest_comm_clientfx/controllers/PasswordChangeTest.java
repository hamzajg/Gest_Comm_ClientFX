package com.xdev.gest_comm_clientfx.controllers;

import com.xdev.gest_comm_clientfx.i18n.Messages;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the client half of "Change password" (specs/login-profile-settings
 * US4): an incomplete or mismatched form never reaches the API, and the
 * server's own refusal explains a rejected password. The request itself is
 * a POST to `/api/me/password`, which the backend suite covers.
 */
class PasswordChangeTest {

    @Test
    void anIncompleteFormIsRefusedLocally() {
        assertEquals("password.missing",
            ProfileCtrl.validationProblem("", "nouveau", "nouveau"));
        assertEquals("password.missing",
            ProfileCtrl.validationProblem("actuel", "", ""));
    }

    @Test
    void theConfirmationMustMatch() {
        assertEquals("password.mismatch",
            ProfileCtrl.validationProblem("actuel", "nouveau", "autre"));
    }

    @Test
    void aCompleteFormIsSent() {
        assertNull(ProfileCtrl.validationProblem(
            "actuel", "nouveau-mot-de-passe", "nouveau-mot-de-passe"));
    }

    @Test
    void theWordingComesFromTheBundle() {
        assertTrue(Messages.get(Locale.FRENCH, "password.mismatch")
            .contains("ne correspondent pas"));
        assertTrue(Messages.get(Locale.ENGLISH, "password.mismatch")
            .contains("do not match"));
    }
}
