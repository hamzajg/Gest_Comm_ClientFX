package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers "Identity endpoint reports the role" (specs/login-profile-settings):
 * the payload the backend sends maps onto the model, and the role decides
 * what the shell may show.
 */
class UserProfileTest {

    @Test
    void mapsTheBackendPayloadFieldByField() throws Exception {
        UserProfile profile = new ObjectMapper().readValue("""
            {"id": 7, "username": "operateur", "firstName": "Ada",
             "lastName": "Lovelace", "email": "ada@example.com",
             "role": "operator"}
            """, UserProfile.class);

        assertEquals(7L, profile.getId());
        assertEquals("operateur", profile.getUsername());
        assertEquals("Ada", profile.getFirstName());
        assertEquals("Lovelace", profile.getLastName());
        assertEquals("ada@example.com", profile.getEmail());
        assertEquals("operator", profile.getRole());
        assertFalse(profile.isAdmin());
    }

    @Test
    void theServerDecidesTheRole() {
        UserProfile admin = new UserProfile();
        admin.setRole("admin");
        UserProfile operator = new UserProfile();
        operator.setRole("operator");

        assertTrue(admin.isAdmin());
        assertEquals("Administrateur", admin.getRoleLabel());
        assertEquals("Opérateur", operator.getRoleLabel());
    }

    @Test
    void displayNameFallsBackToTheLoginThenToADash() {
        UserProfile named = new UserProfile();
        named.setFirstName(" Grace ");
        named.setLastName("Hopper");
        assertEquals("Grace Hopper", named.getDisplayName());

        UserProfile loginOnly = new UserProfile();
        loginOnly.setUsername("operateur");
        assertEquals("operateur", loginOnly.getDisplayName());

        assertEquals("—", new UserProfile().getDisplayName());
    }
}
