# Decision review — industrial-look-feel

Advisory review of `SPEC.md` → Decisions (D1–D4). Resolver: agent.
Verdict only informs the loop; it never blocks a gate or a merge.

## Scorecard

| Lens | Score | Evidence (file:line, test, benchmark) | Action |
|------|-------|----------------------------------------|--------|
| Problem & forces | 5 | Stock Modena is the only stylesheet today (no `*.css` under the repo); six controllers carry inline `-fx-*` colours (`ui/ActionDrawer.java:45-54`, `controllers/HomeScreenCtrl.java:148`) — the "looks like a demo" complaint is structural, not cosmetic. | none |
| Alternatives & tradeoffs | 4 | D1 compares inline patching, a theme library (AtlantaFX/MaterialFX) and an app-owned sheet. Library rejected on dependency + generic identity; inline rejected as three sources of truth. | name the "custom chrome" cost in D2 — done (native title bar kept). |
| Domain fit (DDD) | 4 | Theming is a presentation concern only; no domain or API change (SPEC scope: out). Tokens map to operator density needs (28px rows, 11–12px type). | none |
| Architecture & boundaries | 5 | One `Scene` (`app/StartApp.java:13`) ⇒ one `Theme.install(scene)` call; dialogs are tab-hosted, so they inherit with no wiring. Resource lives beside the FXML and is added to the pom include (D4). | keep `Theme` a pure installer, no state. |
| Testable design | 3 | Only the resource itself is assertable without a JavaFX toolkit (`ThemeTest`); visual acceptance rests on the walkthrough captures. | add the stylesheet assertions now; treat captures as the visual check. |
| Craftsmanship | 4 | Palette as looked-up colours on `.root` (JavaFX's equivalent of variables), component rules grouped by surface, no `!important`-style overrides. | none |
| Decision record | 5 | Four ADR-style blocks with forces, options, consequences and reversibility. | none |

## Verdict

```
VERDICT: ACCEPT
STEELMAN: A single app-owned sheet applied to the one scene is the smallest
change that can restyle every screen at once, and keeping the palette in
looked-up colours makes the whole identity a one-file edit with no new
dependency and no behavioural risk.
SMALLEST NEXT STEP: add a ThemeTest that fails if the sheet, its palette
tokens or the drawer rules disappear, so a future refactor cannot quietly
revert the theme to Modena defaults.
REVISIT TRIGGER: the first request for a second skin (light or high-contrast)
— that is when a library or a theme-manager starts to pay for itself.
```
