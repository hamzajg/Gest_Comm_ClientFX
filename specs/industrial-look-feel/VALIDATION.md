# Validation — industrial-look-feel

## Traceability

| AC | Criterion | Scenarios | Wave(s) | Test evidence |
|----|-----------|-----------|---------|---------------|
| 1 | One stylesheet on the scene, palette defined once | Theme tokens load | 1 | ThemeTest.java |
| 2 | Menu bar, search row and tab strip use the industrial surface | Shell adopts the industrial surface | 1 | ThemeTest.java + capture 01/02 |
| 3 | Tables follow the theme (header, rows, selection, empty area) | Tables follow the theme | 1 | ThemeTest.java + capture 02/04 |
| 4 | Drawer styled expanded and collapsed | Drawer is styled expanded and collapsed | 1 | capture 01/18 |
| 5 | No inline colour in Java or FXML views | Colours live only in the stylesheet | 1 | `grep -rE -- "-fx-(background|text-fill|border|font)-" src/main/java \| grep -v industrial.css` → 0 hits |
| 6 | Usable at the 1024×640 minimum | Window respects the minimum size | 2 | probe `31-min-shell`, `41-rail-min`, `42-form-tab-min`, `44-form-scroll-short` |
| 7 | Suite green | — | 1–2 | `mvn -B package` → 28 tests, 0 failures |
| 8 | Forms/settings/assistant inherit the shell | Forms inherit the theme | 2 | captures 08, 17, 37–39, 42 |

## Scenario coverage

- Scenarios: 7 total, 4 with test evidence (`ThemeTest`), 3 with capture evidence only
- ✓ `Theme tokens load` (@w1)
- ✓ `Shell adopts the industrial surface` (@w1)
- ✓ `Tables follow the theme` (@w1)
- ✓ `Drawer is styled expanded and collapsed` (@w1)
- ✓ `Colours live only in the stylesheet` (@w1)
- ~ `Window respects the minimum size` (@w2) — code only, no resize driver
- ~ `Forms inherit the theme` (@w2) — capture 17 only

## Demo script (manual walkthrough)

1. `mvn javafx:run` with the backend on `127.0.0.1:8000`; the window opens dark with an amber active tab.
2. Click a shortcut chip (Clients) — the table arrives themed, the drawer shows its actions.
3. Collapse the drawer with `›` — the rail keeps the workspace and the table reclaims the width.
4. Menu → Client → Nouveau client — the form is themed, *Enregistrer* is the amber primary action.

## Accepted limitations (not gaps)

- There is no automated visual diff; colour/layout acceptance rests on the
  walkthrough captures rendered by the capture driver, plus computed-fill
  assertions from the state probe.
- States are exercised through pseudo-class injection, not a literal OS
  pointer: accepted because JavaFX applies exactly those pseudo-classes on
  real input, and the probe prints the resulting fills as data.
- Minimum-size behaviour is verified by capture at 1024×640 and by the short
  (1000×300) scroller capture; a live drag-resize was not scripted.

## Outcome follow-up

- Status: not yet measured
- Owner and review date: product owner, next iteration review
- Signal and observed result: reviewer judgement on the captures
- Learning or next action: decide whether a second skin (light/high-contrast)
  is wanted — that is the revisit trigger recorded in DECISION-REVIEW.md

## Walkthrough — live backend, 2026-11-20

Backend: PyGestComm `runserver` on `127.0.0.1:8000` (current `master`).
Driver: `tools/Screenshots.java`, now installing `Theme` on every capture
scene so the shots match the running app.

| Check | How | Observed |
|---|---|---|
| Build + suite | `mvn -B package` | BUILD SUCCESS, `Tests run: 28, Failures: 0` |
| Stylesheet in the build output | the compiled `views` package on the classpath | `industrial.css` present (14 KB) |
| Shell | capture `01-bienvenue.png` | dark menu bar, inset search field, amber-topped active tab, drawer with `ACTIONS` caption |
| Tables | capture `02-clients.png` | dark header row, 28px rows, flat empty fill, monospace status `9 clients chargé(s).` |
| Many tabs | capture `04-devis.png` | six tabs, selected tab marked amber and bold |
| Rail | capture `18-drawer-collapsed.png` | 30px rail, `‹` toggle, table spans the freed width |
| Forms | capture `17-form-tab.png` | form capped at 900px, amber *Enregistrer*, themed fields |
| Inline colours | grep over `src/main/java` | 0 hits outside `industrial.css` |

Before/after: the Modena captures are kept beside the new ones in the
workspace scratch folder as `01-bienvenue-modena.png` and
`02-clients-modena.png`.

### State probe — `tools/ThemeStates.java`, 2026-11-20

A real pointer cannot be scripted into a JavaFX window from CI, so the probe
renders the shell at **1024×640** and sets the same `hover` / `pressed` /
`focused` pseudo-classes JavaFX applies on real input, printing the computed
fills so the states are checked as data as well as pixels.

| Check | Probe | Observed | Verdict |
|---|---|---|---|
| Tab hover | `32-tab-hover.png` + computed fill | rest `#1f252b` → hover `#252c33` | **bug found and fixed** — a short `.tab:hover` lost to the full base selector and never painted; the hover rule now repeats the full selector |
| Drawer hover | `33-drawer-hover.png` | button lightens to `-surface-700`, amber border | ok |
| Drawer pressed | `34-drawer-pressed.png` + fill | `#12161a` dark inset, amber-dim border, amber label | **bug found and fixed** — generic `.button:pressed` was invisible against the drawer surface |
| Drawer focus ring | `40-drawer-focus.png` | amber 1px ring | **bug found and fixed** — no focus rule existed for drawer buttons |
| Search focus | `35-search-focus.png` | amber border + soft glow | ok |
| Row selection | `36-row-selected.png` | amber wash, amber left bar, white text | ok |
| Rail at minimum size | `41-rail-min.png` | 30px rail, `‹` visible, table spans the freed width | ok |
| Form in a tab at minimum size | `42-form-tab-min.png` | 7 rows + actions visible, no clipping | ok |
| Tall forms at 1000×520 | `37`–`39-form-min-*.png` | client/article/devis forms fit; no clipped buttons | ok |

Not verified: hover/pressed with a literal OS pointer (pseudo-classes are set
programmatically), and any colour-blind contrast audit.

## Wave 2 — minimum size and dialog/assistant polish (2026-11-20)

| Check | How | Observed |
|---|---|---|
| Minimum size honoured | `StartApp` sets 1024×640; shell probe `31-min-shell.png` | menu bar, search row, tabs, table, drawer and status all fit at 1024×640 |
| Rail at minimum size | `41-rail-min.png` | 30px rail, `‹` reachable, table spans the freed width |
| Form in a tab at minimum size | `42-form-tab-min.png` | 7 rows + actions + drawer visible, no clipping |
| Short window scrolls instead of clipping | `44-form-scroll-short.png` (form in the tab scroller at 1000×300) | vertical scrollbar appears, action row reachable by scrolling |
| Form labels | `07-config`, `42-form-tab-min` | muted 12px labels, 16px titles, amber primary action |
| Assistant | `16-assistant.png` | amber *Poser*, muted hint, bordered transcript surface |
| Settings | `07-config.png` | styled title, muted field labels, primary *Enregistrer* |
| French empty state | `43-empty-table-fr.png` (filter matching nothing) | `Aucun contenu dans la table` + `0 clients chargé(s).` — the English JavaFX placeholder is gone |

Implementation notes: form tabs are hosted in a `ScrollPane` (`tab-scroll`,
`fitToWidth`), and `StartApp.main` sets `Locale.FRANCE` so JavaFX's own
strings (table placeholder, default dialog buttons) match the French UI. Every
client date is an ISO text field, so the locale change cannot alter date
parsing.

Not verified in wave 2: scrolling with a real wheel/trackpad (the scrollbar is
proven by the capture), and hover/pressed states on the assistant and settings
buttons.
