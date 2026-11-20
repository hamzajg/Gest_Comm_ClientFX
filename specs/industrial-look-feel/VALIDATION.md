# Validation — industrial-look-feel

## Traceability

| AC | Criterion | Scenarios | Wave(s) | Test evidence |
|----|-----------|-----------|---------|---------------|
| 1 | One stylesheet on the scene, palette defined once | Theme tokens load | 1 | ThemeTest.java |
| 2 | Menu bar, search row and tab strip use the industrial surface | Shell adopts the industrial surface | 1 | ThemeTest.java + capture 01/02 |
| 3 | Tables follow the theme (header, rows, selection, empty area) | Tables follow the theme | 1 | ThemeTest.java + capture 02/04 |
| 4 | Drawer styled expanded and collapsed | Drawer is styled expanded and collapsed | 1 | capture 01/18 |
| 5 | No inline colour in Java or FXML views | Colours live only in the stylesheet | 1 | `grep -rE -- "-fx-(background|text-fill|border|font)-" src/main/java \| grep -v industrial.css` → 0 hits |
| 6 | Usable at the 1024×640 minimum | Window respects the minimum size | 2 | `StartApp.setMinWidth/Height`; not exercised by hand |
| 7 | Suite green | — | 1–2 | `mvn -B package` → 28 tests, 0 failures |
| 8 | Forms/settings/assistant inherit the shell | Forms inherit the theme | 2 | capture 08 and 17 |

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

## Gaps

- No automated visual diff: acceptance for colour/layout rests on the 18
  walkthrough captures rendered by the capture driver (kept outside this
  repository, in the workspace scratch area).
- Minimum-size behaviour and hover/pressed pseudo-states were not exercised by
  hand; they are declared in the stylesheet and in `StartApp`.

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

Not verified: hover/pressed/focus visual states on a live pointer, window
resize to the 1024×640 floor, and any colour-blind contrast audit.
