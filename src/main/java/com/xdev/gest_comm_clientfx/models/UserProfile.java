package com.xdev.gest_comm_clientfx.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The signed-in account, as `/api/me` returns it.
 *
 * One field name per backend key (camelCase on both sides); the role is
 * derived server-side from Django's `is_staff` and is never sent by the
 * client (specs/login-profile-settings D1).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String role;

    /** True when the account may administer the application. */
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    /** Name to show in the profile and the shell, falling back to the login. */
    public String getDisplayName() {
        String first = firstName == null ? "" : firstName.trim();
        String last = lastName == null ? "" : lastName.trim();
        String full = (first + " " + last).trim();
        if (!full.isEmpty()) {
            return full;
        }
        return username == null || username.isBlank() ? "—" : username;
    }

    /** French label for the role, as the profile tab shows it. */
    public String getRoleLabel() {
        return isAdmin() ? "Administrateur" : "Opérateur";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
