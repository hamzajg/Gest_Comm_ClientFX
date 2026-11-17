package com.xdev.gest_comm_clientfx.api;

import java.util.prefs.Preferences;

/**
 * Connection settings for the PyGestComm backend, persisted in the
 * user's Java Preferences store (OS-backed — no plaintext config file).
 */
public class ApiConfig {

    public static final String DEFAULT_BASE_URL = "http://localhost:8000";

    private static final Preferences PREFS =
        Preferences.userNodeForPackage(ApiConfig.class);

    private String baseUrl = DEFAULT_BASE_URL;
    private String username = "";
    private String password = "";
    private String refreshToken = "";

    public static ApiConfig load() {
        ApiConfig config = new ApiConfig();
        config.baseUrl = PREFS.get("baseUrl", DEFAULT_BASE_URL);
        config.username = PREFS.get("username", "");
        config.password = PREFS.get("password", "");
        config.refreshToken = PREFS.get("refreshToken", "");
        return config;
    }

    public void save() {
        PREFS.put("baseUrl", baseUrl);
        PREFS.put("username", username);
        PREFS.put("password", password);
        PREFS.put("refreshToken", refreshToken);
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
