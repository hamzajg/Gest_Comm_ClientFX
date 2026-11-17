# Gates: evidence-walkthrough

> Preconditions for phase transitions. Each `## G<n> — <title>` block is a
> gate. `ship_loop` refuses to advance past a phase while a blocking gate is
> pending. Definitions live here; status and evidence live in `LOOP.md`.

## G1 — Spec contract recorded
- Before: plan
- Type: artifact
- Owner: agent
- Resolver: agent
- Blocking: yes
- Check: file:specs/evidence-walkthrough/SPEC.md
- Why: scope (in/out) and acceptance criteria must be frozen before the
  wave is recorded as delivered.

## G2 — Client build green
- Before: validate
- Type: quality
- Owner: agent
- Resolver: agent
- Blocking: yes
- Check: command:mvn -B -q package
- Why: validation requires mechanical evidence — the tool compiles against
  target/classes and the app must still build.

## G3 — No tracked image binaries
- Before: finish
- Type: technical
- Owner: agent
- Resolver: agent
- Blocking: yes
- Check: command:git ls-files 'docs/**/*.png' 'specs/**/*.png'
- Why: the spec's core promise is that evidence is generated, not tracked;
  the check must return empty.
