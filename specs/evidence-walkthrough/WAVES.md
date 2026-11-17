# Waves — evidence-walkthrough

Each wave IS an iteration: a time-boxed, independently shippable
increment — one branch, one PR, green tests, demoable outcome.
Do not start wave N+1 until wave N merges.

## Wave 1 — iteration 1

Scenarios proven by this wave:
- Regenerates every screen capture
- No image binaries are tracked
- Walkthrough maps captures to screens

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US1 | As a maintainer, I want regenerable screen evidence, so that delivery claims can be re-verified without trusting stale binaries | M | — | tools/Screenshots.java + gitignored output dir + spec record; PNGs untracked and purged from history. |
