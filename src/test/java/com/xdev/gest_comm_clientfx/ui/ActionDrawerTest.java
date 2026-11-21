package com.xdev.gest_comm_clientfx.ui;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers "Role gates destructive actions" (specs/login-profile-settings US8):
 * an operator's drawer offers the read/creation actions only, an
 * administrator's keeps them all. Presentation only — the API still decides
 * what a token may do.
 */
class ActionDrawerTest {

    private static final ActionDrawer.Action REFRESH =
        new ActionDrawer.Action("Actualiser", () -> { });
    private static final ActionDrawer.Action DELETE =
        new ActionDrawer.Action("Supprimer", () -> { }, true);
    private static final ActionDrawer.Action PIN =
        new ActionDrawer.Action("Épingler", () -> { });

    private static List<String> labels(List<ActionDrawer.Action> actions) {
        return actions.stream()
            .map(ActionDrawer.Action::label)
            .collect(Collectors.toList());
    }

    @Test
    void anOperatorDoesNotSeeAdminActions() {
        List<String> visible = labels(ActionDrawer.visible(
            List.of(REFRESH, DELETE, PIN), false));

        assertEquals(List.of("Actualiser", "Épingler"), visible);
        assertFalse(visible.contains("Supprimer"));
    }

    @Test
    void anAdministratorKeepsEverything() {
        List<String> visible = labels(ActionDrawer.visible(
            List.of(REFRESH, DELETE, PIN), true));

        assertTrue(visible.contains("Supprimer"));
        assertEquals(3, visible.size());
    }
}
