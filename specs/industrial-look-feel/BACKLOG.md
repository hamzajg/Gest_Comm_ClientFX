# Backlog: industrial-look-feel

Items are USER STORIES in the parseable format below — keep the heading
shape `## US<n> — <story>` and the field lines intact so `wave_plan` can
parse them. Write the story as: "As a <role>, I want <goal>, so that
<value>" — the title after the dash IS the story.

## US1 — As a commercial operator, I want the whole window to share one industrial surface style, so that the app feels like a tool rather than a demo
- Scenarios: @w1
- Size: M
- Notes: stylesheet tokens, `Theme.install`, menu bar, search row, tab strip.

## US2 — As a commercial operator, I want dense tables that stay readable on a dark surface, so that I can scan hundreds of lines without eye strain
- Scenarios: @w1
- Size: M
- Notes: header/row/selection/status styling, sort arrow accent, empty fill.

## US3 — As a commercial operator, I want the action drawer to look like part of the workspace in both states, so that collapsing it never breaks the visual rhythm
- Scenarios: @w1
- Size: S
- Notes: expanded panel, 30px rail, ghost action buttons.

## US4 — As a commercial operator, I want forms and dialogs to inherit the same look, so that creating a record doesn't drop me into a different product
- Scenarios: @w2
- Depends: US1
- Size: M
- Notes: labels, fields, combos, date pickers, section headers, buttons.

## US5 — As a developer, I want colours to live only in the stylesheet, so that a future palette change is one file
- Scenarios: @w1
- Size: S
- Notes: drop inline `-fx-*` colours from `ActionDrawer` and the welcome panel.
