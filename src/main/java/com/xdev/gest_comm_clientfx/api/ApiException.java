package com.xdev.gest_comm_clientfx.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Error talking to the PyGestComm backend (network or HTTP status).
 *
 * A 4xx/5xx answer is a normal outcome of the API (a client still
 * referenced by documents cannot be deleted, a paid invoice is read-only),
 * so the backend's own explanation is carried alongside the status instead
 * of being flattened into one string. Callers phrase it for the user; see
 * {@code controllers.ApiMessages}.
 */
public class ApiException extends RuntimeException {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** HTTP status, or 0 when the failure was transport/parse level. */
    private final int statusCode;
    /** Backend explanation, or null when the response carried none. */
    private final String detail;

    public ApiException(String message) {
        this(message, null, 0, null);
    }

    public ApiException(String message, Throwable cause) {
        this(message, cause, 0, null);
    }

    public ApiException(String message, Throwable cause, int statusCode,
                        String detail) {
        super(message, cause);
        this.statusCode = statusCode;
        this.detail = detail;
    }

    /**
     * Build from a non-2xx response: extracts the DRF {@code detail} (or the
     * first field error) and keeps the raw body as a last-resort message.
     */
    public static ApiException fromResponse(String url, int status,
                                            String body) {
        String detail = extractDetail(body);
        String message = "HTTP " + status + " from " + url
            + (detail != null ? " — " + detail : "");
        return new ApiException(message, null, status, detail);
    }

    public int getStatusCode() {
        return statusCode;
    }

    /** Backend explanation for the failure, or the raw body when not JSON. */
    public String getDetail() {
        if (detail != null && !detail.isBlank()) {
            return detail;
        }
        return getMessage();
    }

    private static String extractDetail(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(body);
            if (root.isObject()) {
                JsonNode detailNode = root.get("detail");
                if (detailNode != null && detailNode.isTextual()) {
                    return detailNode.asText();
                }
                var fields = root.fields();
                while (fields.hasNext()) {
                    var entry = fields.next();
                    JsonNode value = entry.getValue();
                    String text = value.isArray() && !value.isEmpty()
                        ? value.get(0).asText()
                        : value.asText();
                    if (text != null && !text.isBlank()) {
                        return entry.getKey() + " : " + text;
                    }
                }
            }
        } catch (Exception ignored) {
            // Not JSON (an HTML error page, a proxy message): use it as-is.
        }
        return abbreviate(body);
    }

    private static String abbreviate(String body) {
        String trimmed = body.trim();
        return trimmed.length() > 200 ? trimmed.substring(0, 200) + "…"
            : trimmed;
    }
}
