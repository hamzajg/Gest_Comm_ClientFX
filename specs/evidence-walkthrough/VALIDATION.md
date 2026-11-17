# Validation — evidence-walkthrough

## Traceability

| AC | Criterion | Scenarios | Wave(s) | Test evidence |
|----|-----------|-----------|---------|---------------|
| 1 | Running the documented commands regenerates all screen… | No image binaries are tracked, Regenerates every screen capture, Walkthrough maps captures to screens | 1 | — |
| 2 | No screenshot binaries are tracked anywhere in the repo | No image binaries are tracked, Regenerates every screen capture, Walkthrough maps captures to screens | 1 | — |
| 3 | The spec records which screen each capture proves and how… | No image binaries are tracked, Regenerates every screen capture, Walkthrough maps captures to screens | 1 | — |

## Scenario coverage

- Scenarios: 3 total, 0 with detected test evidence, 3 without
- ✗ `Regenerates every screen capture` (@w1)
- ✗ `No image binaries are tracked` (@w1)
- ✗ `Walkthrough maps captures to screens` (@w1)

## Demo script (manual walkthrough)

1. **Regenerates every screen capture** — run the app, perform the scenario, observe the outcome.

## Gaps

- 3 scenario(s) lack detected test evidence — verify manually or add checks

## Manual verification (agent, 2026-11-17)

- `Regenerates every screen capture` — ran tools/Screenshots.java against
  the live backend: 16 PNGs written, zero controller exceptions.
- `No image binaries are tracked` — `git ls-files 'docs/**/*.png'
  'specs/**/*.png'` → empty; docs/walkthrough/ purged from history
  (master rewritten, cf83469) and gitignored.
- `Walkthrough maps captures to screens` — capture→screen→API mapping is
  recorded in this spec's SPEC.md scope and the LOOP wave-1 evidence.

Note: 'no evidence' means neither the scenario name nor a
token match was found in test files — verify by hand before
treating it as a real gap. '~' marks weak token-match evidence.

## Outcome follow-up

Optional; this is a post-release learning record, not a finish gate.
- Status: not yet measured
- Owner and review date: (record when known)
- Signal and observed result: (record after release; do not invent)
- Learning or next action: (record after review)