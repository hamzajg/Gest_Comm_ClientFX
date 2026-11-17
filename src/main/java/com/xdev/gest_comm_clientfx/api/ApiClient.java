package com.xdev.gest_comm_clientfx.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.BonLiv;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.Facture;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP/JSON client for the PyGestComm REST API.
 *
 * Backend contract (see PyGestComm AGENTS.md): every /api/* route requires
 * HTTP Basic auth; collections return JSON arrays; documents accept
 * nested writes. This client currently covers the read paths the UI
 * needs — list endpoints — plus a generic post() for later screens.
 */
public class ApiClient {

    private final ApiConfig config;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    // JWT state: the access token is memory-only (1 h lifetime); the
    // refresh token is persisted via ApiConfig so restarts skip login.
    private String accessToken;

    public ApiClient(ApiConfig config) {
        this.config = config;
    }

    public List<Client> listClients() throws ApiException {
        return listClients(null);
    }

    public List<Client> listClients(String search) throws ApiException {
        return getList("/api/client", Client.class, search);
    }

    public List<Article> listArticles() throws ApiException {
        return listArticles(null);
    }

    public List<Article> listArticles(String search) throws ApiException {
        return getList("/api/article", Article.class, search);
    }

    public List<Devis> listDevis() throws ApiException {
        return listDevis(null);
    }

    public List<Devis> listDevis(String search) throws ApiException {
        return getList("/api/devis", Devis.class, search);
    }

    public Devis createDevis(Object payload) throws ApiException {
        return post("/api/devis", payload, Devis.class);
    }

    public List<Facture> listFactures() throws ApiException {
        return listFactures(null);
    }

    public List<Facture> listFactures(String search) throws ApiException {
        return getList("/api/facture", Facture.class, search);
    }

    public Facture createFacture(Object payload) throws ApiException {
        return post("/api/facture", payload, Facture.class);
    }

    public Facture convertDevis(Long devisId, Object payload)
        throws ApiException {
        return post("/api/devis/" + devisId + "/convertir", payload,
            Facture.class);
    }

    public List<BonLiv> listBonLivs() throws ApiException {
        return listBonLivs(null);
    }

    public List<BonLiv> listBonLivs(String search) throws ApiException {
        return getList("/api/bonliv", BonLiv.class, search);
    }

    public BonLiv createBonLiv(Object payload) throws ApiException {
        return post("/api/bonliv", payload, BonLiv.class);
    }

    public <T> T put(String path, Object payload, Class<T> responseType)
        throws ApiException {
        String body = send("PUT", path, payload);
        try {
            return mapper.readValue(body, responseType);
        } catch (IOException e) {
            throw new ApiException("Cannot parse response from " + path, e);
        }
    }

    public void delete(String path) throws ApiException {
        send("DELETE", path, null);
    }

    public void patch(String path, Object payload) throws ApiException {
        send("PATCH", path, payload);
    }

    /** POST /api/assistant/ask — returns {intent, answer, data}. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> askAssistant(String question)
        throws ApiException {
        String body = send("POST", "/api/assistant/ask",
            Map.of("question", question));
        try {
            return mapper.readValue(body, Map.class);
        } catch (IOException e) {
            throw new ApiException(
                "Cannot parse assistant response", e);
        }
    }

    public <T> List<T> getList(String path, Class<T> elementType)
        throws ApiException {
        return getList(path, elementType, null);
    }

    public <T> List<T> getList(String path, Class<T> elementType,
                               String search) throws ApiException {
        String url = path;
        if (search != null && !search.isBlank()) {
            url += "?search=" + URLEncoder.encode(
                search.trim(), StandardCharsets.UTF_8);
        }
        String body = send("GET", url, null);
        try {
            return mapper.readValue(body, mapper.getTypeFactory()
                .constructCollectionType(List.class, elementType));
        } catch (IOException e) {
            throw new ApiException("Cannot parse response from " + path, e);
        }
    }

    public <T> T post(String path, Object payload, Class<T> responseType)
        throws ApiException {
        String body = send("POST", path, payload);
        try {
            return mapper.readValue(body, responseType);
        } catch (IOException e) {
            throw new ApiException("Cannot parse response from " + path, e);
        }
    }

    public String send(String method, String path, Object payload)
        throws ApiException {
        HttpResponse<String> response = sendOnce(
            method, path, payload, true);
        if (response.statusCode() == 401) {
            // Access token expired/rejected — rotate and retry once.
            refreshTokens();
            response = sendOnce(method, path, payload, false);
        }
        if (response.statusCode() >= 400) {
            throw new ApiException("HTTP " + response.statusCode()
                + " from " + url(path) + " — "
                + abbreviate(response.body()));
        }
        return response.body();
    }

    /**
     * One HTTP round-trip. When {@code withAuth} is true, obtains/attaches
     * a Bearer token (token endpoints themselves never carry one).
     */
    private HttpResponse<String> sendOnce(String method, String path,
                                          Object payload, boolean withAuth)
        throws ApiException {
        String url = url(path);
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(
                    URI.create(url))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10));
            if (withAuth && !path.startsWith("/api/token")) {
                builder.header("Authorization",
                    "Bearer " + ensureAccessToken());
            }
            if (payload != null) {
                builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(payload)));
            } else if (!"GET".equals(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            return http.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ApiException("Cannot reach " + url
                + " — is PyGestComm running?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request to " + url + " interrupted", e);
        }
    }

    private String url(String path) {
        return config.getBaseUrl().replaceAll("/+$", "") + path;
    }

    private synchronized String ensureAccessToken()
        throws ApiException {
        if (accessToken == null) {
            // Prefer the persisted refresh token (survives restarts and
            // password changes); fall back to credential login inside
            // refreshTokens() when it's missing/expired.
            refreshTokens();
        }
        return accessToken;
    }

    /**
     * Exchange stored credentials for a fresh token pair at
     * POST /api/token. The new refresh token is persisted — the server
     * rotates and blacklists it on every refresh.
     */
    private synchronized void obtainTokens() throws ApiException {
        HttpResponse<String> response = sendOnce(
            "POST", "/api/token",
            Map.of("username", config.getUsername(),
                   "password", config.getPassword()),
            false);
        if (response.statusCode() >= 400) {
            throw new ApiException("HTTP " + response.statusCode()
                + " from " + url("/api/token")
                + " — check username/password — "
                + abbreviate(response.body()));
        }
        storeTokens(response.body());
    }

    /**
     * Rotate the token pair via /api/token/refresh. If the refresh token
     * is expired/revoked, fall back to a full credential login.
     */
    private synchronized void refreshTokens() throws ApiException {
        String refresh = config.getRefreshToken();
        if (refresh == null || refresh.isBlank()) {
            obtainTokens();
            return;
        }
        HttpResponse<String> response;
        try {
            response = sendOnce("POST", "/api/token/refresh",
                Map.of("refresh", refresh), false);
        } catch (ApiException e) {
            obtainTokens();
            return;
        }
        if (response.statusCode() >= 400) {
            obtainTokens();
            return;
        }
        storeTokens(response.body());
    }

    private void storeTokens(String body) throws ApiException {
        try {
            Map<?, ?> tokens = mapper.readValue(body, Map.class);
            accessToken = (String) tokens.get("access");
            Object newRefresh = tokens.get("refresh");
            if (newRefresh != null) {
                config.setRefreshToken((String) newRefresh);
                config.save();
            }
        } catch (IOException e) {
            throw new ApiException("Cannot parse token response", e);
        }
    }

    private static String abbreviate(String body) {
        return body != null && body.length() > 200
            ? body.substring(0, 200) + "…" : body;
    }
}
