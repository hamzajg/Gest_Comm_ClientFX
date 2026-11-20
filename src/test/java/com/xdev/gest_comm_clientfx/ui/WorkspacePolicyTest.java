package com.xdev.gest_comm_clientfx.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkspacePolicyTest {

    @Test
    void listAndUtilityPanelsAreSingleInstance() {
        for (String key : new String[]{WorkspacePolicy.WELCOME,
                WorkspacePolicy.CLIENTS, WorkspacePolicy.ARTICLES,
                WorkspacePolicy.DEVIS, WorkspacePolicy.FACTURES,
                WorkspacePolicy.BONLIVS, WorkspacePolicy.CONFIG}) {
            assertTrue(WorkspacePolicy.isSingleInstance(key),
                key + " must reuse its tab");
        }
    }

    @Test
    void formTabsAreNeverReused() {
        String first = WorkspacePolicy.formKey("Nouveau client", 1);
        String second = WorkspacePolicy.formKey("Nouveau client", 2);
        assertFalse(WorkspacePolicy.isSingleInstance(first));
        assertFalse(WorkspacePolicy.isSingleInstance(second));
        assertTrue(first.startsWith("form:"));
        assertEquals(false, first.equals(second));
    }

    @Test
    void onlyListPanelsScopeTheSearch() {
        assertEquals(true, WorkspacePolicy.isList(WorkspacePolicy.CLIENTS));
        assertEquals(true, WorkspacePolicy.isList(WorkspacePolicy.BONLIVS));
        assertFalse(WorkspacePolicy.isList(WorkspacePolicy.CONFIG));
        assertFalse(WorkspacePolicy.isList(WorkspacePolicy.WELCOME));
    }

    @Test
    void everyPanelHasAFrenchTitle() {
        assertEquals("Bienvenue", WorkspacePolicy.title(WorkspacePolicy.WELCOME));
        assertEquals("Bons de livraison",
            WorkspacePolicy.title(WorkspacePolicy.BONLIVS));
        assertEquals("Paramètres", WorkspacePolicy.title(WorkspacePolicy.CONFIG));
    }
}
