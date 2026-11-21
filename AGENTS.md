# AGENTS.md — Gest_Comm_ClientFX

Guidance for AI agents (and humans) working in this repository.

## Project

Gest_Comm_ClientFX is a **JavaFX desktop client** (French/English UI) for the
*gestion commerciale* back end **PyGestComm** (Django REST). Early stage:
the window, config screen, live Clients/Articles/Devis/Factures list
tabs, create/edit/delete forms and statut actions for all four resources, and
devis→facture conversion, an assistant panel and bons de livraison
work against the API — the full declared domain is wired.
Stack: Java 17, JavaFX 17 (via Maven, `javafx-controls`/`javafx-fxml`),
Jackson databind, `java.net.http`, Maven, FXML views.

## Commands

Verified 2026-11-17 against CI (`.github/workflows/ci.yml`) and README.

| Task | Command |
|---|---|
| Build | `mvn -B package` (CI: JDK 17) |
| Run app | `mvn javafx:run` |
| Tests | `mvn -B package` runs JUnit 5 tests (29) |

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
    ApiClient.java      java.net.http + Jackson; JWT Bearer auth
                        request; list*/create*/put/delete/patch for all four
                        resources, convertDevis(), getList()/post()/send()
    ApiConfig.java      baseUrl + credentials in java.util.prefs
                        (Preferences.userNodeForPackage — no plaintext file)
    ApiException.java   transport/HTTP error type (RuntimeException) —
                        status code + backend `detail` parsed from DRF
                        bodies (`fromResponse`)
  controllers/
    Display             what a screen shows for an absent value:
                        fields → empty (never the literal "null"),
                        cells/labels → "—" (legacy null-on-screen bug)
    ApiMessages         phrasing for API failures, in the chosen language
                        (409 → "Suppression impossible : …", 400 → "Action
                        refusée : …"), shared by every controller
    HomeScreenCtrl      @FXML: home shell — table columns, menu actions,
                        drawer wiring + convertSelectedDevis; loads run
                        on a background thread, UI updates via
                        Platform.runLater. Every panel opens through
                        TabWorkspace (a tab, never a Stage)
    ConfigScreenCtrl    @FXML: server URL only (the account is entered on
                        the login gate), save, test-connection via /api/me
    ProfileCtrl         @FXML: profile tab — name, login, e-mail and the
                        server-derived role, sign out back to the gate
    NouveauClientCtrl   @FXML: client create form → POST /api/client
    NouvelArticleCtrl   @FXML: article create form → POST /api/article
    NouveauDevisCtrl    @FXML: quote form — client/article pickers, line
                        editor (LigneDraft rows), live HT/TVA/TTC,
                        POST /api/devis with nested lignes
    NouvelFactureCtrl   @FXML: invoice form — same line editor (reuses
                        NouveauDevisCtrl.LigneDraft), optional devis
                        picker that prefills client/date/lines, POST
                        /api/facture with nested lignes
  ui/
    WorkspacePolicy     tab keys, title keys, which panels are singletons,
                        which scope the search (no JavaFX — unit tested)
    TabWorkspace        opens/closes tabs, reuses singleton panels,
                        pin (★ removes the close button)
    ActionDrawer        right-hand pane with the active tab's actions;
                        expands/collapses to a 30px rail, never hides
    Theme               installs views/industrial.css on the single app
                        Scene — the sheet is the only place a colour lives
    AppPreferences      workstation theme (dark/light) and language
                        (fr/en) in Java Preferences; injected node is
                        used by tests
    Views               FXMLLoader factory that attaches the current
                        messages bundle, so FXML text="%key" localizes
  i18n/
    Messages            messages.properties (fr) + messages_en.properties;
                        get(key, args), Locale-aware for tests
  models/               Article + Client + Devis + LigneDevis + Facture +
                        LigneFacture + BonLiv + LigneBonLiv POJOs
      views/                *.fxml layouts (HomeScreen, ConfigScreen,
                        LoginView, ProfileView) + the messages bundles
pom.xml                 JavaFX + Jackson deps + resources-plugin copies
                        views/*.fxml onto the classpath
```

### API contract (client ↔ PyGestComm backend)

The backend is **PyGestComm** (`../PyGestComm`), not the old ADF `Gest_Comm`
repo. Every `/api/*` route requires JWT auth (PyGestComm ADR 0008);
`ApiClient` exchanges username/password once at `POST /api/token` for
`{access, refresh}`, sends `Authorization: Bearer <access>`, refreshes
via `/api/token/refresh` on 401 (rotated refresh token persisted in
Preferences; falls back to credential login when expired).
`ApiConfig`. Used: `GET`/`POST`/`PUT`/`DELETE`/`PATCH` on `/api/client`, `/api/article`,
`/api/devis`, `/api/facture`, `/api/bonliv`, plus
`POST /api/devis/<id>/convertir` (Devis → Facture
menu action; backend requires statut `valide`). Create dialogs send `Map`
payloads — only non-empty optional fields are included. Document writes
send `lignes: [{article?, designation, quantite, prixUnitaire, remise,
tva}]` — numeric values as strings are accepted. List filters (`?search=`/`?statut=`/`?client=`) are not consumed yet.

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
  All backend resources now have client POJOs — keep them in sync.
  `BonLivClient` was removed once the real `BonLiv` model landed.
- French UI strings; package root `com.xdev.gest_comm_clientfx`.

## Landmines

- **Default branch is `master`, not `main`.**
- Everything is wired: all five resources support list/create/edit/
  delete, full edit (header + lignes via PUT) and statut transitions,
  devis→facture conversion, and the assistant dialog.
- Create dialogs send `Map<String,Object>` payloads, not the POJOs —
  keep it that way for writes (explicit keys, no Jackson serialization
  surprises); POJOs are for reads.
- `ConfigScreen.fxml`'s controller must stay
  `com.xdev.gest_comm_clientfx.controllers.ConfigScreenCtrl` (plural
  `controllers`) — it was once broken by a singular path.
- `mvn javafx:run` handles the module path; plain `java -jar` won't work
  without the plugin's setup.
- `specs/` holds ship-loop spec artifacts — `specs/evidence-walkthrough/`
  records the screen-evidence feature; `tools/Screenshots.java`
  regenerates captures into gitignored `docs/walkthrough/` (never commit
  PNGs).
- The username + rotating refresh token live in the OS preferences store;
  the password is **never** persisted (used for the sign-in exchange only)
  and `ApiConfig.load()` erases one left by an older release. The access
  token is memory-only. Bearer tokens are plaintext without TLS —
  acceptable on a LAN, needs TLS before any real deployment (PyGestComm
  ADR 0008).
- README's status table is accurate and current — trust it over
  assumptions.

## Agent rules

- `StartApp.main` sets the default locale from the saved language
  preference: JavaFX's own strings (table placeholder, default dialog
  buttons) must match the UI. Every date in the client is an ISO text field,
  not a `DatePicker` — keep it that way, or the locale affects date parsing.
- UI text lives in `views/messages.properties` (French base) and
  `messages_en.properties`. FXML uses `text="%key"` and must be loaded
  through `ui.Views` (or a loader with `Messages.resourceBundle()`); a
  plain `new FXMLLoader(url)` cannot resolve `%key`. Every view — login,
  shell, profile, settings, the record forms and the assistant — is
  externalized; table headers, list status and confirmation text go through
  `Messages` from Java. `MessagesBundleTest` enforces key parity, that every
  FXML key exists and that no view hard-codes a visible label.
- `Messages` resolves the requested locale then the base bundle, never the
  JVM default: the stock fallback made a French request load the English
  file on an English machine.
- Theme and language are workstation preferences (`ui.AppPreferences`), not
  server state: switching either applies immediately, and the language
  switch rebuilds the workspace scene (open tabs are lost).
- Form tabs are opened inside a `ScrollPane` (`.tab-scroll`, `fitToWidth`) so
  a form taller than the window scrolls instead of clipping its buttons.
- Colours belong in `views/industrial.css` (palette tokens on `.root`),
  never in `setStyle` or FXML `style=` — inline styles beat the stylesheet
  in JavaFX. Java only adds style classes; `ui/Theme` attaches the sheet.
- Branch from `master`; name branches `feat/*`, `fix/*`, `chore/*`, `docs/*`.
- Never add AI/assistant attribution to commit messages, PR bodies,
  reviews or comments: no "generated with …" footer, no `Co-Authored-By:`
  agent trailer, no robot-emoji signature line. A commit message ends at its
  own last line, with no trailer block.
- Docs-only changes still go through a PR; never commit to `master` directly.
- Verify before claiming: `mvn -B package` must pass — quote the test count
  it reports.
- CSS state rules must repeat the full selector: a short `.tab:hover` loses
  to the base `.tab-pane > … > .tab` rule and silently never paints. Probe
  states with `tools/ThemeStates.java` (sets hover/pressed/focused
  pseudo-classes at 1024×640) before claiming a state works.
- Keep changes minimal and consistent with surrounding code. No drive-by
  refactors.
- Update AGENTS.md / docs/ARCHITECTURE.md when controllers get wired, the
  API surface grows, or the backend contract changes (coordinate with
  PyGestComm).
