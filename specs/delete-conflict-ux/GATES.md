# Gates: delete-conflict-ux

> Preconditions for phase transitions. Each `## G<n> — <title>` block is a
> gate. `ship_loop` refuses to advance past a phase while a blocking gate is
> pending. Definitions live here; status and evidence live in `LOOP.md`.
>
> Gate phases: `spec` (before writing the spec), `plan` (before planning
> waves), `wave:<n>` (before starting wave n), `validate` (before validation),
> `finish` (before closing the feature).

## G1 — Design decisions challenged
- Before: plan
- Type: artifact
- Owner: agent
- Resolver: agent
- Blocking: yes
- Check: file:specs/delete-conflict-ux/DECISION-REVIEW.md
- Why: Every wave inherits the feature's architecture choices (where error
  parsing lives, which layer owns the wording). The challenger review must
  exist before wave planning locks the design in.
