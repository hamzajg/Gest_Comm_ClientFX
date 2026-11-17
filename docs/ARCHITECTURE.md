# Architecture — Gest_Comm_ClientFX

Current-state map. Descriptive, not aspirational; see `docs/adr/` for why.

## System shape

```
StartApp (JavaFX Application)
   └─ HomeScreen ──loads──► views/HomeScreen.fxml
   │                          menus: Application / Article / Client /
   │                          Devis / Facture / Help; tabs Bienvenue /
   │                          Clients / Articles / Devis / Factures /
   │                          Bons de livraison
   ├─ ConfigScreen ──loads──► views/ConfigScreen.fxml (settings form)
   ▼
controllers/  HomeScreenCtrl — @FXML tabs/tables/menus; loads API data
              on a background thread → Platform.runLater; launches the
              create dialogs with onSaved → refresh callbacks
              ConfigScreenCtrl — settings form; save + test connection
              NouveauClientCtrl / NouvelArticleCtrl — create forms that
              POST Map payloads and close on success
              NouveauDevisCtrl — quote form: client/article ComboBoxes,
              LigneDraft line editor, live totals, POST /api/devis
              NouvelFactureCtrl — invoice form reusing LigneDraft, plus
              an optional devis picker that prefills client/date/lines;
              POST /api/facture
              HomeScreenCtrl.convertSelectedDevis — POST
              /api/devis/<id>/convertir on the selected row
api/          ApiClient (java.net.http + Jackson, JWT Bearer auth)
              ApiConfig (Java Preferences) · ApiException
models/       Article + Client + Devis + LigneDevis + Facture +
              LigneFacture + BonLiv + LigneBonLiv POJOs with accessors
        │
        ▼  HTTP/JSON + Authorization: Bearer <jwt>
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

Defined and working for the full commercial document chain, including
row-level edit (PUT), delete (DELETE with confirm) and statut PATCH:

`GET`/`POST` on `/api/client`, `/api/article`, `/api/devis`, `/api/facture`
plus `POST /api/devis/<id>/convertir` (devis→facture) — all with JWT
Bearer auth (PyGestComm ADR 0008): tokens from `POST /api/token`,
rotating refresh via `/api/token/refresh`. Writes send `Map` payloads with camelCase
keys; document writes nest `lignes`; PUT replaces all lines when provided (article-linked or free-text lines);
reads deserialize into the POJOs. Filters (`?search=`/`?statut=`/
`?client=`), detail routes and the assistant endpoint are unused so far —
`BonLivClient` stays empty until that screen is designed.
