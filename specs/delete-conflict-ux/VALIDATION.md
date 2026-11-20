# Validation — delete-conflict-ux

## Traceability

| AC | Criterion | Scenarios | Wave(s) | Test evidence |
|----|-----------|-----------|---------|---------------|
| 1 | `ApiException` exposes the HTTP status and the backend… | A blocked delete explains why in French, API errors expose status and backend detail, Home screen has no placeholder text and no empty filler column | 1 | ApiExceptionTest.java |
| 2 | A delete blocked by the backend shows `Suppression… | A blocked delete explains why in French, API errors expose status and backend detail, Home screen has no placeholder text and no empty filler column | 1 | ApiExceptionTest.java |
| 3 | The Home screen header has no `Text Here` placeholder and… | A blocked delete explains why in French, API errors expose status and backend detail, Home screen has no placeholder text and no empty filler column | 1 | ApiExceptionTest.java |
| 4 | `mvn -B package` stays green (existing 8 tests + new ones)… | A blocked delete explains why in French, API errors expose status and backend detail, Home screen has no placeholder text and no empty filler column | 1 | ApiExceptionTest.java |

## Scenario coverage

- Scenarios: 3 total, 2 with detected test evidence, 1 with weak (token-match) evidence only, 0 without
- ✓ `A blocked delete explains why in French` (@w1)
- ✓ `API errors expose status and backend detail` (@w1)
- ~ `Home screen has no placeholder text and no empty filler column` (@w1)

## Demo script (manual walkthrough)

1. **A blocked delete explains why in French** — run the app, perform the scenario, observe the outcome.
2. **API errors expose status and backend detail** — run the app, perform the scenario, observe the outcome.
3. **Home screen has no placeholder text and no empty filler column** — run the app, perform the scenario, observe the outcome.

## Gaps

- none — spec is fully traced

Note: 'no evidence' means neither the scenario name nor a
token match was found in test files — verify by hand before
treating it as a real gap. '~' marks weak token-match evidence.

## Outcome follow-up

Optional; this is a post-release learning record, not a finish gate.
- Status: not yet measured
- Owner and review date: (record when known)
- Signal and observed result: (record after release; do not invent)
- Learning or next action: (record after review)
## Walkthrough — live backend, 2026-11-20

Backend: PyGestComm `runserver` on `127.0.0.1:8000` (the sibling feature's
409 contract is present in that worktree). Driver: `tools/Screenshots.java`
— it boots the real controllers, calls the real `ApiClient` (JWT login, list
all five resources) and renders every screen; captures are attached to the
wave evidence bundle.

| Check | How | Observed |
|---|---|---|
| Build + suite | `mvn -B package` | BUILD SUCCESS, `Tests run: 14, Failures: 0` |
| Login + five list endpoints | `Screenshots` run | `data: 4 clients, 3 articles, 5 devis, 7 factures, 3 bonlivs` |
| Home header | capture `02-clients.png` | no `Text Here` label; search field remains |
| Table width | capture `02-clients.png` | five labelled columns fill the table, no filler column |
| Devis form | capture `10-nouveau-devis.png` | client/article pickers, live totals HT/TVA/TTC |
| Assistant | capture `16-assistant.png` | dialog answers over `POST /api/assistant/ask` |
| 409 phrasing | `ApiExceptionTest` | status 409 + backend `detail` → `Suppression impossible : …` |

Not verified: clicking *Supprimer* on a referenced row inside a running
window (the modal confirmation needs a human or a UI driver). The wording
path is unit-tested and the backend's 409 is asserted in the sibling spec;
the column-resize half of the UI scenario is verified on the capture, which
is why `ship_validate` reports it as token-match rather than test-detected.
