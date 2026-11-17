# ADR 0002 — PyGestComm API client and first wired screens

Date: 2026-11-17
Status: accepted

## Context

The client was a skeleton: empty controllers, inert menus, no HTTP layer.
The backend decision has also moved — the live backend is **PyGestComm**
(Django/DRF), not the ADF `Gest_Comm` repo whose generated REST shapes
were never stable. PyGestComm now exposes the full domain with Basic
auth (its ADRs 0003–0006).

## Decision

1. **`api/` package, JDK-built transport.** `ApiClient` wraps
   `java.net.http.HttpClient` + Jackson `ObjectMapper` — no RESTEasy/
   OkHttp dependency. Jackson databind was added to `pom.xml` (the only
   new dependency).
2. **Basic auth from `ApiConfig`.** Credentials (baseUrl, username,
   password) live in `java.util.prefs.Preferences` — OS-backed, nothing
   plaintext in the repo. Sent as `Authorization: Basic …` on every
   request, matching PyGestComm's auth model.
3. **Controllers actually wired.** `HomeScreenCtrl` injects the tab
   pane, tables and status labels; menu items call `onAction` handlers.
   `ConfigScreenCtrl` implements the settings form (save + test
   connection). Network calls run on background threads with
   `Platform.runLater` updates — no service layer, KISS.
4. **Read paths first.** Clients and Articles get list tabs fed by
   `GET /api/client` and `GET /api/article`. Write screens, devis/facture
   models (`BonLivClient`/`FactureClient`) and document workflows are
   explicitly deferred — the shells stay empty rather than guessed.
5. **`Article` keeps snake_case Java fields** bridged by `@JsonProperty`
   to the API's camelCase JSON — preserves the ADF-aligned naming while
   mapping cleanly; `Client` needed no mapping (names already match).

## Consequences

- The product loop is real for the first time: backend up → configure
  credentials → *Liste des clients/articles* shows live data.
- `mvn -B package` builds and 5 JUnit tests pass, including JSON-mapping
  tests that pin the backend payload shape.
- Basic auth means credentials cross the wire on every call — fine on a
  LAN, TLS required before real deployment (same caveat as PyGestComm).
- When write screens land, `ApiClient.post()` and the devis/facture
  endpoints are already there; models for documents will need nested
  `lignes` handling — design then, not now.
