# Gates: login-profile-settings

> Preconditions for phase transitions. Each `## G<n> — <title>` block is a
> gate. `ship_loop` refuses to advance past a phase while a blocking gate is
> pending. Definitions live here; status and evidence live in `LOOP.md`.
>
> Gate phases: `spec` (before writing the spec), `plan` (before planning
> waves), `wave:<n>` (before starting wave n), `validate` (before validation),
> `finish` (before closing the feature).
>
> Gate types: decision | artifact | external | technical | quality | data.
> Resolver: `human` (needs a person to decide/provide something) or `agent`
> (the agent can satisfy it). Check: `manual`, `file:<repo-relative-path>`, or
> `command:<exact command>` (the agent runs it and records the output as
> evidence — the oracle never executes it).

## G1 — Design decisions challenged
- Before: plan
- Type: artifact
- Owner: agent
- Resolver: agent
- Blocking: yes
- Check: file:specs/login-profile-settings/DECISION-REVIEW.md
- Why: D1 (role from `is_staff`), D3 (stop persisting the password) and D5
  (language by scene rebuild) are inherited by every wave; the challenger
  review must exist before the plan locks them in.

## G2 — Both suites are green before wave 1
- Before: wave:1
- Type: quality
- Owner: agent
- Resolver: agent
- Blocking: yes
- Check: command:cd src/GestCommProject && python manage.py test
- Why: the client wave is built against the backend identity endpoint; a red
  backend suite would hide whether a failure is new. The client suite
  (`mvn -B package`) is run in the same evidence step.
