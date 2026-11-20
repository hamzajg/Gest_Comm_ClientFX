# Spec: delete-conflict-ux

> Spec-driven development artifact. This file is the contract: implementation
> is done when every acceptance criterion maps to a scenario in
> `EXAMPLES.feature` and every scenario has a passing check.

## Problem

The desktop client renders every API failure the same way:
`"Erreur : HTTP 409 from http://localhost:8000/api/client/3 — {json…}"`.
Two things are wrong for a French-speaking operator:

1. The backend's actionable `detail` ("A client used by 2 devis cannot be
   deleted") is buried inside a truncated raw JSON blob, after an English
   status line, in a 200-character cut.
2. Normal business outcomes (a record that other documents still reference,
   a paid invoice that is read-only) look like crashes.

The HomeScreen also still carries a leftover `Text Here` placeholder in the
header and, because the tables use the default unconstrained resize policy,
an unlabelled filler column that eats the right edge of every grid.

## Users

- Desktop operators (French UI) deleting rows from the Clients, Articles,
  Devis, Factures and Bons de livraison tabs.
- The next maintainer: `ApiException` should be the single place a caller
  looks for "what did the backend say".

## Outcome hypothesis

- Expected user outcome: when a delete cannot succeed, the operator reads a
  French sentence naming the blocking documents instead of retrying.
- Opportunity evidence: live capture on 2026-11-20 — deleting a referenced
  client answered 500/`ProtectedError`; after the backend fix the same call
  answers 409 with a `detail`, which the client still printed raw.
- Signal: the row status line for a blocked delete contains the backend
  `detail` (asserted by unit test), and no UI path prints a bare
  `HTTP <code> from <url>` string for 4xx answers.
- Follow-up owner/date: agent, at validation of this feature.

## Scope (in)

- `ApiException` gains `getStatusCode()` and `getDetail()`, parsed from the
  DRF error body (`detail`, or the first field error), falling back to the
  raw body for non-JSON answers.
- `ApiClient` raises those enriched exceptions for every non-2xx answer.
- `HomeScreenCtrl` (mutate/load) and the create dialogs phrase API errors
  in French: `"Suppression impossible : <detail>"` for a 409 on delete,
  `"Action refusée : <detail>"` for a 400, `"Erreur : <detail>"` otherwise.
- Home screen: drop the `Text Here` placeholder label; size table columns to
  fill the table (no empty filler column).
- README/AGENTS.md status text corrected to match what actually ships.
- Live re-capture of the walkthrough screens against PyGestComm.

## Scope (out)

- No change to the backend contract (PyGestComm `protected-delete-conflict`
  delivers the 409 + `detail`).
- No restyling, theming, i18n framework or new screens.
- No change to the dialog layouts beyond the header placeholder.
- No offline mode or retry queue.

## Acceptance criteria

| # | Criterion | Scenario(s) |
|---|-----------|-------------|
| 1 | `ApiException` exposes the HTTP status and the backend `detail`; a 409 delete yields `getStatusCode()==409` and a non-empty `getDetail()` | @w1 |
| 2 | A delete blocked by the backend shows `Suppression impossible : <detail>` in the row status line, never a raw `HTTP 409 from …` blob | @w1 |
| 3 | The Home screen header has no `Text Here` placeholder and the tables show no unlabelled filler column | @w1 |
| 4 | `mvn -B package` stays green (existing 8 tests + new ones) and the app still renders every screen against the live backend | @w1 |

## Decisions

### D1 — Parse DRF error bodies inside `ApiException`, not in each controller

- **Context & forces:** Five controllers and five row actions each catch
  `ApiException` and print `e.getMessage()`. Adding JSON parsing at every
  call site would duplicate the same 15 lines and drift. `ApiException` is
  already the single transport error type.
- **Options considered:**
  1. Parse in each controller (simplest to write once, five copies to keep
     in sync).
  2. Parse in `ApiException` at construction, keeping `getMessage()` as the
     legacy one-line form and adding `getDetail()` (reversible: one class,
     callers can keep using `getMessage()`).
  3. Introduce a separate `ApiError` DTO plus a mapper used by the client —
     more structure, more surface than five resources justify.
- **Decision & why:** Option 2. Status + detail live where the HTTP layer
  already lives; controllers only decide the French phrasing.
- **Consequences:** (+) one parse, one place to fix; tests can assert on
  status/detail without a UI. (−) `ApiException` now depends on Jackson for
  parsing (already a hard dependency) and `getMessage()` keeps a
  raw-ish fallback for non-JSON bodies.
- **Reversibility:** two-way door — additive accessors; no contract change.

### D2 — Phrase the French message at the UI call site, not in the API layer

- **Context & forces:** The same exception means "delete blocked" in one
  place and "save rejected" in another. The API layer must stay
  language-neutral so a future web client can reuse it.
- **Options considered:**
  1. Build the full French sentence inside `ApiException` (one string,
     wrong layer, unusable for any non-French consumer).
  2. Map status → French prefix in the controller that knows the intent
     (small helper in `HomeScreenCtrl`), with `getDetail()` as the payload.
- **Decision & why:** Option 2. Intent belongs to the screen.
- **Consequences:** (+) API layer stays portable; messages read naturally.
  (−) each controller owns a small phrasing helper.
- **Reversibility:** two-way door.
