# AGENTS.md — Gest_Comm_ClientFX

Guidance for AI agents (and humans) working in this repository.

## Project

Gest_Comm_ClientFX is a **JavaFX desktop client** (French UI) for the
*gestion commerciale* back end **PyGestComm** (Django REST). Early stage:
the window, config screen, and live Clients/Articles list tabs work
against the API; write screens and the Bon Liv/Facture menus are still
stubs.
Stack: Java 17, JavaFX 17 (via Maven, `javafx-controls`/`javafx-fxml`),
Jackson databind, `java.net.http`, Maven, FXML views.

## Commands

Verified 2026-11-17 against CI (`.github/workflows/ci.yml`) and README.

| Task | Command |
|---|---|
| Build | `mvn -B package` (CI: JDK 17) |
| Run app | `mvn javafx:run` |
| Tests | `mvn -B package` runs JUnit 5 tests (5) |

Requires: JDK 17 only — JavaFX and Jackson come from Maven. The app also
needs a running PyGestComm backend (`python manage.py runserver` in that
repo) plus credentials configured via *Application → Parametre de
l'application*.
Entry point: `com.xdev.gest_comm_clientfx.app.StartApp` (set as
`mainClass` in the javafx-maven-plugin).

## Architecture

```
src/main/java/com/xdev/gest_comm_clientfx/
  app/
    StartApp.java       Application subclass → shows HomeScreen
    HomeScreen.java     loads views/HomeScreen.fxml via FXMLLoader
    ConfigScreen.java   loads views/ConfigScreen.fxml
  api/
    ApiClient.java      java.net.http + Jackson; Basic auth on every
                        request; listClients()/listArticles()/getList()/
                        post()/send()
    ApiConfig.java      baseUrl + credentials in java.util.prefs
                        (Preferences.userNodeForPackage — no plaintext file)
    ApiException.java   transport/HTTP error type (RuntimeException)
  controllers/
    HomeScreenCtrl      @FXML: tabs (Bienvenue/Clients/Articles), table
                        columns, menu actions; loads run on a background
                        thread, UI updates via Platform.runLater
    ConfigScreenCtrl    @FXML: server URL/user/password fields, save,
                        test-connection
  models/               Article + Client POJOs with getters/setters;
                        BonLivClient / FactureClient — still empty shells
  views/                *.fxml layouts (HomeScreen, ConfigScreen)
pom.xml                 JavaFX + Jackson deps + resources-plugin copies
                        views/*.fxml onto the classpath
```

### API contract (client ↔ PyGestComm backend)

The backend is **PyGestComm** (`../PyGestComm`), not the old ADF `Gest_Comm`
repo. Every `/api/*` route requires HTTP Basic auth (PyGestComm ADR 0006);
`ApiClient` sends `Authorization: Basic base64(user:pass)` from
`ApiConfig`. Used so far: `GET /api/client`, `GET /api/article` (both
return JSON arrays). The backend also has `/api/devis`, `/api/facture`,
`/api/devis/<id>/convertir`, `?search=`/`?statut=`/`?client=` filters, and
`POST /api/assistant/ask` — none consumed yet.

JSON shapes: `Client` field names mirror the backend one-to-one
(camelCase). `Article` keeps legacy snake_case Java fields bridged by
`@JsonProperty` annotations to the API's camelCase keys — don't rename
the Java fields without updating the annotations and tests. Backend
decimals arrive as strings (`"100.00"`) — Jackson handles that into
`BigDecimal`/`Double` automatically.

## Conventions

- JavaFX + FXML: views in `views/*.fxml`, screens as loaders in `app/`,
  controllers in `controllers/` with `@FXML` injection (now actually
  used). Menu actions are wired via `onAction="#method"` in the FXML.
- `.fxml` files live under `src/main/java` and reach the classpath via the
  maven-resources-plugin entry — keep that config if moving files.
- HTTP work never runs on the FX thread — controllers spawn a thread and
  marshal results back with `Platform.runLater` (see `HomeScreenCtrl.load`).
- Models are plain POJOs — French domain names
  (`ref_article`, `prix_vente_unitaire`, `remise_achat`, `tva_*`,
  `codeClient`, `nomPrenomClient`). `Client`/`Article` map the backend
  JSON; keep them in sync when backend models change.
  `BonLivClient`/`FactureClient` remain empty until their backend shape
  is implemented client-side — don't guess fields.
- French UI strings; package root `com.xdev.gest_comm_clientfx`.

## Landmines

- **Default branch is `master`, not `main`.**
- Only *Parametre de l'application*, *Liste des clients* and *Liste des
  articles* are wired — every other menu item is still inert.
- `ConfigScreen.fxml`'s controller must stay
  `com.xdev.gest_comm_clientfx.controllers.ConfigScreenCtrl` (plural
  `controllers`) — it was once broken by a singular path.
- `mvn javafx:run` handles the module path; plain `java -jar` won't work
  without the plugin's setup.
- Credentials live in the OS preferences store, sent as Basic auth —
  fine on a LAN, needs TLS before any real deployment (PyGestComm ADR
  0006).
- README's status table is accurate and current — trust it over
  assumptions.

## Agent rules

- Branch from `master`; name branches `feat/*`, `fix/*`, `chore/*`, `docs/*`.
- Docs-only changes still go through a PR; never commit to `master` directly.
- Verify before claiming: `mvn -B package` must pass; there are 5 tests —
  say so.
- Keep changes minimal and consistent with surrounding code. No drive-by
  refactors.
- Update AGENTS.md / docs/ARCHITECTURE.md when controllers get wired, the
  API surface grows, or the backend contract changes (coordinate with
  PyGestComm).
