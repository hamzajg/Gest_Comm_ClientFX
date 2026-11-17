# Architecture — Gest_Comm_ClientFX

Current-state map. Descriptive, not aspirational; see `docs/adr/` for why.

## System shape

```
StartApp (JavaFX Application)
   └─ HomeScreen ──loads──► views/HomeScreen.fxml
   │                          menus: Application / Article / Client /
   │                          Bon Liv / Facture; tabs Bienvenue /
   │                          Clients / Articles
   ├─ ConfigScreen ──loads──► views/ConfigScreen.fxml (settings form)
   ▼
controllers/  HomeScreenCtrl — @FXML tabs/tables/menus; loads API data
              on a background thread → Platform.runLater; launches the
              create dialogs with onSaved → refresh callbacks
              ConfigScreenCtrl — settings form; save + test connection
              NouveauClientCtrl / NouvelArticleCtrl — create forms that
              POST Map payloads and close on success
api/          ApiClient (java.net.http + Jackson, Basic auth every call)
              ApiConfig (Java Preferences) · ApiException
models/       Article + Client POJOs with accessors; BonLivClient,
              FactureClient still empty shells
        │
        ▼  HTTP/JSON + Authorization: Basic …
PyGestComm (Django/DRF)  /api/client  /api/article  /api/devis
                         /api/facture  /api/assistant/ask  /admin/
```

## Design

- **FXML + loader pattern.** `HomeScreen`/`ConfigScreen` wrap
  `FXMLLoader.load(...)` and expose `getRootView()`; `StartApp` puts the
  root in a `Scene`. Controllers are real `@FXML` classes now — menu items
  use `onAction="#..."`, tabs/tables/labels are injected by `fx:id`.
- **Anemic models + Jackson bridge.** `Article`/`Client` are DTO-shaped
  POJOs. `Client` field names match the backend JSON 1:1; `Article`
  keeps snake_case Java fields with `@JsonProperty` mapping to the API's
  camelCase keys.
- **Thin API layer.** `ApiClient` is a `java.net.http.HttpClient` +
  Jackson `ObjectMapper` wrapper: `getList(path, type)`, `post(path,
  payload, type)`, raw `send(method, path, payload)`. No retry, no
  caching, no offline mode — errors surface as `ApiException`.
- **Config in Java Preferences.** `ApiConfig` stores baseUrl/username/
  password via `Preferences.userNodeForPackage` — OS-backed, no
  plaintext file in the repo or home dir.
- **FX-thread discipline.** All network I/O runs on spawned threads;
  results reach the UI through `Platform.runLater` (`HomeScreenCtrl.load`,
  `ConfigScreenCtrl.testConnection`).

## State

Server-side only. The client keeps no local database — lists are fetched
on demand via *Actualiser* / menu actions. Credentials persist in the
preferences store between runs.

## Contract status

Defined and working for clients/articles: `GET`/`POST /api/client` and
`GET`/`POST /api/article` with Basic auth (PyGestComm ADR 0006). Writes
send `Map` payloads with camelCase keys; reads deserialize into the
POJOs. The backend's detail routes, devis/facture documents, filters
and assistant are unused so far — `BonLivClient`/`FactureClient` stay
empty until those screens are designed.
