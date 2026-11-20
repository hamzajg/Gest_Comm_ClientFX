# Decision review brief — Spec: delete-conflict-ux

- Source: `specs/delete-conflict-ux/SPEC.md (## Decisions)`
- Generated: 2026-11-20T09:04:29Z
- Detected type: ux, architecture
- Readiness: **CHALLENGE** (6/6 record elements)
- This brief is input to judgment, not a verdict. The agent fills the scorecard after reading the code.

**Reversibility:** stated

**Alternatives on record (verify they are credible, not strawmen):**
- ApiException is already the single transport error type. - Options considered: 1.
- Introduce a separate ApiError DTO plus a mapper used by the client — more structure, more surface than five resources justify. - Decision & why: Option 2.
- The API layer must stay language-neutral so a future web client can reuse it. - Options considered: 1.
- Map status → French prefix in the controller that knows the intent (small helper in HomeScreenCtrl), with getDetail() as the payload. - Decision & why: Option 2

## Evidence signals (heuristic — verify before acting)

- `src/main/java/com/xdev/gest_comm_clientfx/api/ApiException.java` — no test file references this code (heuristic)
- `src/main/java/com/xdev/gest_comm_clientfx/api/ApiException.java` — 1 source file(s); not a domain path, so layering checks were skipped
- `src/main/java/com/xdev/gest_comm_clientfx/api/ApiClient.java` — no test file references this code (heuristic)
- `src/main/java/com/xdev/gest_comm_clientfx/api/ApiClient.java` — 1 source file(s); not a domain path, so layering checks were skipped
- `src/main/java/com/xdev/gest_comm_clientfx/controllers/HomeScreenCtrl.java` — no test file references this code (heuristic)
- `src/main/java/com/xdev/gest_comm_clientfx/controllers/HomeScreenCtrl.java` — 1 source file(s); not a domain path, so layering checks were skipped

Inspected: `src/main/java/com/xdev/gest_comm_clientfx/api/ApiException.java`, `src/main/java/com/xdev/gest_comm_clientfx/api/ApiClient.java`, `src/main/java/com/xdev/gest_comm_clientfx/controllers/HomeScreenCtrl.java`

## Design moves to test

- Which user journey does this serve, including empty, error, and slow states?
- What is the accessibility and keyboard path?
- Where does the UI hold state that belongs to the domain?
- Can this be a module inside the current deployable before it becomes a service?
- Which dependency can be inverted so the core owns the interface?
- What is the smallest reversible step that tests the boundary?
- Invert a dependency so the unstable thing depends on the stable thing.
- Make an implicit boundary or invariant explicit before adding mechanism.
- Shrink the decision: can half of it ship independently and reversibly?

## Challenge questions

### 1. Problem & forces
- What user/business outcome does this serve, and what breaks if we do nothing?
- Which quality attributes actually drive it (correctness, latency, cost, operability, compliance, time-to-market)? Rank them.
- Which constraints are fixed (budget, deadline, team, contracts) and which are assumed?
- Who owns this decision, and who must live with the consequences?
**Good looks like:** The dominant force is named, and "do nothing" is treated as a real option.

### 2. Alternatives & tradeoffs
- What are at least two credible alternatives, including the simplest thing that could work?
- For each: what does it make easy, what does it make hard, and what does it cost to reverse?
- Is this a one-way or two-way door? If one-way, what evidence justifies it?
- What would have to be true for the chosen option to be wrong?
**Good looks like:** A tradeoff matrix names winners and losers; reversibility is explicit; no strawman options.

### 3. Domain fit (DDD)
- Which bounded context owns this concept, and is the boundary drawn around language or deployment?
- Which terms are overloaded across contexts, and where is the translation (anti-corruption layer)?
- What are the aggregates, their invariants, and the transaction boundary? Does one transaction span aggregates?
- Are domain events the right integration mechanism, or is this a synchronous dependency?
- Does the model carry behavior, or is it an anemic data bag?
**Good looks like:** Ubiquitous language is explicit, invariants are protected at the boundary, external models are translated.

### 4. Architecture & boundaries
- Which direction do dependencies point? Can the core be tested without the framework, DB, or network?
- What is the smallest boundary that isolates this change (module, port, package)?
- What happens when each external dependency is slow, down, or returns garbage?
- Where does data ownership live, and is there a second writer?
- What is the operational story: config, migrations, rollout, rollback, observability?
**Good looks like:** Policy depends on nothing; mechanisms are adapters; failure and migration paths are named.

### 5. Testable design
- Where are the seams? Can time, randomness, IO, and external calls be injected?
- Which behaviors get fast unit tests, which need integration, which need contract tests?
- Does each acceptance criterion map to an observable check, or only to implementation details?
- Are the tests deterministic and independent (no shared state, no sleeps)?
- What is the failure signal when this decision turns out wrong in production?
**Good looks like:** Behavior-first tests, deterministic seams, scenario-to-test traceability, a named failure signal.

### 6. Craftsmanship
- Is this the smallest reversible step, or a big-bang change?
- Are names from the domain, or from the framework/table?
- Is the error model explicit (typed failures vs exceptions vs silent nulls)?
- Is there speculative abstraction (interfaces with one implementation, config nobody sets)?
- Is the migration path documented and rollback-able?
**Good looks like:** Small steps, intention-revealing names, explicit errors, no speculative generality, documented migration.

### 7. Decision record
- Are status, owner, and date recorded?
- Are consequences (positive and negative) written down?
- Is there a revisit trigger (metric, date, event)?
- Are links to evidence, code, and related ADRs present?
**Good looks like:** A future reader can tell why, what was traded, and when to revisit.

## Scorecard (agent fills; 1 = poor, 5 = strong, evidence required)

| Lens | Score | Evidence (file:line, test, benchmark) | Action |
|------|-------|----------------------------------------|--------|
| Problem & forces | 5 | Live capture 2026-11-20: row status printed `Erreur : HTTP 409 from http://localhost:8000/api/client/3 — {json}`; `ApiException.java` carries no status and `HomeScreenCtrl.mutate` prints `e.getMessage()` verbatim | none |
| Alternatives & tradeoffs | 4 | SPEC D1 compares per-controller parsing / `ApiException` parsing / separate DTO with a stated reason for rejecting each | none |
| Domain fit (DDD) | 4 | API layer stays language-neutral; the French phrasing lives in the screen that knows the intent | none |
| Architecture & boundaries | 4 | `ApiClient` raises one enriched exception for every non-2xx answer; `java.net.http` + Jackson already dependencies, so no new boundary. Concern: `ApiException` gains a parsing concern | keep parsing ~15 lines and delegate the message text to callers |
| Testable design | 5 | `ApiException` can be constructed from a status + body in a plain JUnit test; the UI phrasing helper is a pure static function | add one JUnit test per scenario |
| Craftsmanship | 4 | Risk of five copies of the French phrasing in five controllers | extract one helper (`ApiMessages`) instead of copying |
| Decision record | 5 | D1/D2 carry forces, options, decision, consequences; both marked two-way doors | none |

## Verdict (agent fills)

```
VERDICT: ACCEPT
STEELMAN: Enriching the one existing transport exception keeps the change additive and testable, while the UI keeps its own language so a future web client reuses the transport layer untouched.
SMALLEST NEXT STEP: Put the French phrasing in one small helper used by every controller, so the wording cannot drift across five call sites.
REVISIT TRIGGER: a second client (web/mobile) needs the same messages, or a localization requirement appears.
```

Run the review with `/challenge-decision` and the `decision-challenger`
subagent. It is advisory: it never blocks a gate or a merge.
