# Waves — delete-conflict-ux

Each wave IS an iteration: a time-boxed, independently shippable
increment — one branch, one PR, green tests, demoable outcome.
Do not start wave N+1 until wave N merges.

## Wave 1 — iteration 1

Scenarios proven by this wave:
- A blocked delete explains why in French
- API errors expose status and backend detail
- Home screen has no placeholder text and no empty filler column

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US1 | As a desktop operator, I want a refused action explained in French, so that I know what to change | S | — | ApiException status+detail, French phrasing in HomeScreenCtrl and the dialogs, header/column cleanup, docs. |
