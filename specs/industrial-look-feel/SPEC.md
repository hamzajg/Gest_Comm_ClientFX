# Spec: industrial-look-feel

> Spec-driven development artifact. This file is the contract: implementation
> is done when every acceptance criterion maps to a scenario in
> `EXAMPLES.feature` and every scenario has a passing check.

## Problem

Gest_Comm_ClientFX ships with the stock JavaFX Modena stylesheet: light grey
chrome, 12px system text, blue focus rings and default table/tab furniture.
Every screen — the tab strip, the five record tables, the action drawer and
the create/edit forms — therefore looks like an unbranded demo rather than a
workspace a commercial user sits in for a full day. The tab workspace landed
first; the surface now has to catch up so the product reads as a deliberate,
industrial-grade tool instead of a Java sample.

## Users

- Primary: the *gestionnaire commercial* (office operator) who keeps the
  client/devis/facture tables open all day, often on a laptop, and works
  mostly from the keyboard.
- Secondary: the developer reviewing the app in screenshots and CI captures.

## Outcome hypothesis

- Expected user outcome: the app reads as a purpose-built commercial
  workstation; screen-recording/screenshot reviews no longer flag it as
  "default JavaFX".
- Opportunity evidence: assumption — the visual pass is requested directly by
  the product owner after the tab-workspace iteration; no usage telemetry
  exists in this desktop app.
- Signal: reviewer judgement on the walkthrough captures (no numeric target
  invented); no regressions in the 25-test suite, layout still usable at
  1200×700 and at the 1024×640 minimum.
- Follow-up owner/date: product owner at the next iteration review.

## Scope (in)

- Paths: `src/main/java/com/xdev/gest_comm_clientfx/views/industrial.css`,
  `src/main/java/com/xdev/gest_comm_clientfx/ui/Theme.java`,
  `src/main/java/com/xdev/gest_comm_clientfx/ui/ActionDrawer.java`,
  `src/main/java/com/xdev/gest_comm_clientfx/app/StartApp.java`,
  `src/main/java/com/xdev/gest_comm_clientfx/controllers/HomeScreenCtrl.java`,
  `src/test/java/com/xdev/gest_comm_clientfx/ui/ThemeTest.java`,
  `tools/ThemeStates.java`, `tools/Screenshots.java`, `pom.xml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/Assistant.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/ConfigScreen.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/NouveauClient.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/NouvelArticle.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/NouveauDevis.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/NouvelFacture.fxml`,
  `src/main/java/com/xdev/gest_comm_clientfx/views/NouveauBonLiv.fxml`
- One app-owned stylesheet (tokens + component rules) bundled as a classpath
  resource and applied to the single application `Scene`.
- Industrial palette: graphite surfaces, steel borders, a single amber accent,
  high-contrast typography with uppercase micro-labels.
- Restyle of every surface visible in the main window: menu bar, search row,
  tab strip, tables (header, rows, selection), status labels, action drawer
  (expanded and rail), scrollbars, focus ring, welcome panel.
- Removal of hard-coded inline `-fx-*` colours so the stylesheet is the only
  place a colour lives.
- Dialogs/forms inherit the same tokens automatically (they open as tabs in
  the same scene).

## Scope (out)

- Custom window chrome / undecorated stage, splash screen, animations,
  icon-set design, dark/light theme switching, high-DPI asset work.
- Any change to controllers' behaviour, navigation, API calls or data.
- Backend (PyGestComm) changes.
- Third-party theme libraries.

## Acceptance criteria

| # | Criterion | Scenario(s) |
|---|-----------|-------------|
| 1 | A single stylesheet is on the scene and defines the palette tokens once | Theme tokens load |
| 2 | Menu bar, search row and tab strip use the industrial surface/border treatment | Shell adopts the industrial surface |
| 3 | Table headers, rows, selection and empty area follow the theme | Tables follow the theme |
| 4 | The action drawer is styled in both expanded and collapsed rail states | Drawer is styled expanded and collapsed |
| 5 | No inline colour remains in Java or FXML views | Colours live only in the stylesheet |
| 6 | Layout stays usable at the 1024×640 minimum window size | Window respects the minimum size |
| 7 | The suite still builds and passes | (quality gate G2) |
| 8 | Forms, assistant and settings inherit the shell surfaces | Forms inherit the theme |

## Decisions

### D1 — Theme = one app-owned CSS file + a token block, no theme library

- Context & forces: JavaFX ships Modena; its look is the reason the app reads
  as a demo. The client has no CSS today and six controllers set ad-hoc
  colours inline, so any restyle risks three sources of truth.
- Options: (a) keep inline `setStyle` and patch screens one by one;
  (b) take AtlantaFX/MaterialFX and restyle on top; (c) one app-owned
  stylesheet with a token block, applied to the single scene.
- Decision & why: (c). The app has exactly one `Scene` (`StartApp`), so one
  stylesheet reaches every tab-hosted screen — no per-dialog wiring. It adds
  no dependency and stays fully reversible by deleting the resource.
- Consequences: (+) one place to change a colour, no dependency, dialogs get
  the theme for free; (−) we own ~500 lines of CSS and must keep JavaFX
  pseudo-classes (hover/pressed/selected/focused) correct by hand.
- Reversibility: two-way door (remove the `Theme.install` call); revisit if
  the app ever needs two skins at runtime.

### D2 — Dark graphite base with one amber accent, native title bar kept

- Context & forces: "modern rich industrial" is a mood, not a spec; operators
  stare at dense tables all day, so contrast and calm matter more than
  novelty. Undecorated windows would mean re-implementing drag/resize/macOS
  traffic lights for no product value.
- Options: (a) light industrial grey; (b) dark graphite + amber accent;
  (c) dark + custom chrome.
- Decision & why: (b) with the OS title bar. Dark surfaces make the amber
  selection/status signals pop and give the dense tables a control-room feel;
  native chrome keeps window behaviour untouched.
- Consequences: (+) strong identity, readable tables; (−) dark UI needs
  careful secondary-text contrast (kept ≥ 4.5:1 by using `#a7b2bd` on
  `#1f252b`); print/screenshot of light-only flows is not offered.
- Reversibility: two-way door — the token block is the only place to swap.

### D3 — Style classes over inline styles; keep Java edits minimal

- Context & forces: `ActionDrawer` and the welcome panel set colours in Java,
  which would fight the stylesheet (inline wins over CSS).
- Options: (a) leave inline styles and out-specify them; (b) move every colour
  to CSS classes and drop the inline ones; (c) stylesheet only for new nodes.
- Decision & why: (b). Inline styles beat stylesheets in JavaFX, so they must
  go; the visible Java change is limited to `getStyleClass().add(...)` plus
  the removal of the `setStyle` calls.
- Consequences: (+) theme is the single source of truth; (−) tests that
  asserted inline styling must be updated (none do).
- Reversibility: two-way door.

### D4 — Bundle the stylesheet next to the FXML views

- Context & forces: FXML and controllers live in the `views` package under
  the Java source tree because the pom copies `**/*.fxml` from there; this
  project has no separate Maven resources root at all.
- Options: (a) introduce a second resource root and move the views;
  (b) put the stylesheet beside the FXML and widen the pom include to
  `**/*.css`.
- Decision & why: (b) — one resource root, one loader convention
  (`Theme.STYLESHEET`), no pom restructuring.
- Consequences: (+) consistent with existing loading; (−) resources live in a
  Java source tree, which is unusual outside this project (documented in
  AGENTS.md).
- Reversibility: two-way door.
