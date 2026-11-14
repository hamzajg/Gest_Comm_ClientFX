# AGENTS.md — Gest_Comm_ClientFX

Guidance for AI agents (and humans) working in this repository.

## Project

Gest_Comm_ClientFX is a **JavaFX desktop client** (French UI) for the
*gestion commerciale* back end `Gest_Comm` (ADF/BC4J REST). Skeleton stage:
the window, menus, and domain model shells exist; controllers and screens
are stubs — menus open nothing, no data loads.
Stack: Java 17, JavaFX 17 (via Maven, `javafx-controls`/`javafx-fxml`),
Maven, FXML views.

## Commands

Verified 2026-11-13 against CI (`.github/workflows/ci.yml`) and README.

| Task | Command |
|---|---|
| Build | `mvn -B package` (CI: JDK 17) |
| Run app | `mvn javafx:run` |
| Tests | none yet |

Requires: JDK 17 only — JavaFX comes from Maven, no separate SDK.
Entry point: `com.xdev.gest_comm_clientfx.app.StartApp` (set as
`mainClass` in the javafx-maven-plugin).

## Architecture

```
src/main/java/com/xdev/gest_comm_clientfx/
  app/
    StartApp.java       Application subclass → shows HomeScreen
    HomeScreen.java     loads views/HomeScreen.fxml via FXMLLoader
    ConfigScreen.java   empty placeholder
  controllers/          HomeScreenCtrl, ConfigScreenCtrl — EMPTY stubs
  models/               Article (fields only, no getters/setters),
                        Client / BonLivClient / FactureClient — empty shells
  views/                *.fxml layouts (HomeScreen, ConfigScreen)
pom.xml                 JavaFX deps + resources-plugin copies views/*.fxml
                        onto the classpath
```

### API contract (client ↔ Gest_Comm backend)

**None yet.** There is no HTTP client code — `grep` for http/rest/api in
`src/main/java` finds nothing. The intended backend is Gest_Comm's ADF
`/rest/*` resources (Article view object only, unstable/generated). When
implemented, the client must consume ADF's generated JSON shapes — treat
that contract as undefined until first real calls land.

## Conventions

- JavaFX + FXML: views in `views/*.fxml`, screens as loaders in `app/`,
  controllers in `controllers/` (intended `@FXML` wiring — not yet used).
- `.fxml` files live under `src/main/java` and reach the classpath via the
  maven-resources-plugin entry — keep that config if moving files.
- Models are anemic field shells — French domain names
  (`ref_article`, `prix_vente_unitaire`, `remise_achat`, `tva_*`). Match
  the backend's vocabulary; the sibling repo `Gest_Comm`/`PyGestComm`
  share the domain.
- French UI strings; package root `com.xdev.gest_comm_clientfx`.

## Landmines

- **Default branch is `master`, not `main`.**
- Everything interactive is inert: menus have no `onAction`, controllers
  are empty, `Article` has no getters/setters — don't assume features work.
- No tests, no HTTP layer, no persistence — "the app runs" ≠ "the app does
  anything".
- `mvn javafx:run` handles the module path; plain `java -jar` won't work
  without the plugin's setup.
- README's status table is accurate and current — trust it over assumptions.

## Agent rules

- Branch from `master`; name branches `feat/*`, `fix/*`, `chore/*`, `docs/*`.
- Docs-only changes still go through a PR; never commit to `master` directly.
- Verify before claiming: `mvn -B package` must pass; there are no tests —
  say so.
- Keep changes minimal and consistent with surrounding code. No drive-by
  refactors.
- Update AGENTS.md / docs/ARCHITECTURE.md when controllers get wired, an
  HTTP client is added, or the backend contract firms up.
