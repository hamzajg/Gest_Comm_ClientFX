package com.xdev.gest_comm_clientfx.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xdev.gest_comm_clientfx.models.Article;
import com.xdev.gest_comm_clientfx.models.Client;
import com.xdev.gest_comm_clientfx.models.Devis;
import com.xdev.gest_comm_clientfx.models.Facture;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
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

    public ApiClient(ApiConfig config) {
        this.config = config;
    }

    public List<Client> listClients() throws ApiException {
        return getList("/api/client", Client.class);
    }

    public List<Article> listArticles() throws ApiException {
        return getList("/api/article", Article.class);
    }

    public List<Devis> listDevis() throws ApiException {
        return getList("/api/devis", Devis.class);
    }

    public Devis createDevis(Object payload) throws ApiException {
        return post("/api/devis", payload, Devis.class);
    }

    public List<Facture> listFactures() throws ApiException {
        return getList("/api/facture", Facture.class);
    }

    public Facture createFacture(Object payload) throws ApiException {
        return post("/api/facture", payload, Facture.class);
    }

    public Facture convertDevis(Long devisId, Object payload)
        throws ApiException {
        return post("/api/devis/" + devisId + "/convertir", payload,
            Facture.class);
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
        String body = send("GET", path, null);
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
        String url = config.getBaseUrl().replaceAll("/+$", "") + path;
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(
                    URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuth())
                .timeout(Duration.ofSeconds(10));
            if (payload != null) {
                builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(
                        mapper.writeValueAsString(payload)));
            } else if (!"GET".equals(method)) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            HttpResponse<String> response = http.send(
                builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new ApiException("HTTP " + response.statusCode()
                    + " from " + url + " — " + abbreviate(response.body()));
            }
            return response.body();
        } catch (IOException e) {
            throw new ApiException("Cannot reach " + url
                + " — is PyGestComm running?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request to " + url + " interrupted", e);
        }
    }

    private String basicAuth() {
        String credentials = config.getUsername() + ":"
            + config.getPassword();
        return "Basic " + Base64.getEncoder().encodeToString(
            credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static String abbreviate(String body) {
        return body != null && body.length() > 200
            ? body.substring(0, 200) + "…" : body;
    }
}
