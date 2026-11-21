package com.xdev.gest_comm_clientfx.api;

import java.util.prefs.Preferences;

/**
 * Connection settings for the PyGestComm backend, persisted in the
 * user's Java Preferences store (OS-backed — no plaintext config file).
 *
 * The username and the rotating refresh token are persisted; the password
 * is not (specs/login-profile-settings D3). It is used for the sign-in
 * exchange only and lives in this object for the session, never on disk.
 */
public class ApiConfig {

    public static final String DEFAULT_BASE_URL = "http://localhost:8000";

    /** Key older releases wrote the plaintext password under. */
    static final String PASSWORD_KEY = "password";

    private final Preferences prefs;

    private String baseUrl = DEFAULT_BASE_URL;
    private String username = "";
    private String password = "";
    private String refreshToken = "";

    public ApiConfig() {
        this(Preferences.userNodeForPackage(ApiConfig.class));
    }

    /** Preferences-node injection, so the persistence rules can be tested. */
    ApiConfig(Preferences prefs) {
        this.prefs = prefs;
    }

    public static ApiConfig load() {
        return load(Preferences.userNodeForPackage(ApiConfig.class));
    }

    static ApiConfig load(Preferences prefs) {
        ApiConfig config = new ApiConfig(prefs);
        config.baseUrl = prefs.get("baseUrl", DEFAULT_BASE_URL);
        config.username = prefs.get("username", "");
        config.refreshToken = prefs.get("refreshToken", "");
        // Upgrade path: an earlier release stored the password here. Remove
        // it the first time the new client reads the node, so the secret is
        // not left behind by the upgrade itself.
        if (prefs.get(PASSWORD_KEY, null) != null) {
            prefs.remove(PASSWORD_KEY);
        }
        return config;
    }

    public void save() {
        prefs.put("baseUrl", baseUrl);
        prefs.put("username", username);
        prefs.put("refreshToken", refreshToken);
        // Defence in depth: nothing in this class may ever persist it.
        prefs.remove(PASSWORD_KEY);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /** In-memory only: the sign-in exchange uses it, {@link #save()} drops it. */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /** Rotating JWT refresh token — persisted so restarts skip login. */
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
