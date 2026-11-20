# Waves — industrial-look-feel

Each wave IS an iteration: a time-boxed, independently shippable
increment — one branch, one PR, green tests, demoable outcome.
Do not start wave N+1 until wave N merges.

## Wave 1 — iteration 1

Scenarios proven by this wave:
- Theme tokens load
- Shell adopts the industrial surface
- Tables follow the theme
- Drawer is styled expanded and collapsed
- Colours live only in the stylesheet

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US1 | As a commercial operator, I want the whole window to share one industrial surface style, so that the app feels like a tool rather than a demo | M | — | stylesheet tokens, `Theme.install`, menu bar, search row, tab strip. |
| US2 | As a commercial operator, I want dense tables that stay readable on a dark surface, so that I can scan hundreds of lines without eye strain | M | — | header/row/selection/status styling, sort arrow accent, empty fill. |
| US3 | As a commercial operator, I want the action drawer to look like part of the workspace in both states, so that collapsing it never breaks the visual rhythm | S | — | expanded panel, 30px rail, ghost action buttons. |
| US5 | As a developer, I want colours to live only in the stylesheet, so that a future palette change is one file | S | — | drop inline `-fx-*` colours from `ActionDrawer` and the welcome panel. |

## Wave 2 — iteration 2

Scenarios proven by this wave:
- Window respects the minimum size
- Forms inherit the theme

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US4 | As a commercial operator, I want forms and dialogs to inherit the same look, so that creating a record doesn't drop me into a different product | M | US1 | labels, fields, combos, date pickers, section headers, buttons. |
