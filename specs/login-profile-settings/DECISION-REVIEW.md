# Decision review — login-profile-settings

Advisory review of `SPEC.md` → Decisions (D1–D6). Resolver: agent.
Verdict only informs the loop; it never blocks a gate or a merge.

## Scorecard

| Lens | Score | Evidence (file:line, test, benchmark) | Action |
|------|-------|----------------------------------------|--------|
| Problem & forces | 5 | The force is real and recorded: `api/ApiConfig.java:37-41` writes `username` **and** `password` to Preferences, `ApiClient.ensureAccessToken()` silently exchanges them, and `app/StartApp.java:14` builds the workspace before any identity exists. `GestCommProject/GestCommProject/urls.py` has no `/api/me`, so role is not knowable. | none |
| Alternatives & tradeoffs | 4 | D1 compares custom user, groups and `is_staff`; D3 compares keeping the password, memory-only and login-every-launch; D5/D6 were added after this review's first draft asked how a dead session behaves. | D1 now states the enforcement boundary explicitly (see Verdict). |
| Domain fit (DDD) | 4 | Identity is an application concern, not a domain aggregate; the spec keeps `User` out of the commercial model and adds no field to `Client`/`Devis`. | none |
| Architecture & boundaries | 4 | `Theme` stays a pure installer and gains a skin selector instead of a manager; the login scene reuses the same sheet. `ApiClient` grows a `SessionExpired` callback (D5) rather than every screen handling 401. | keep the callback single, on the client instance that owns the tokens. |
| Testable design | 4 | Backend scenarios land as `APITestCase` (`GestCommApp/tests.py` already has the `AuthAPITestCase` base and 56 tests); client-side assertions are pure-data tests (`ApiConfig`, role mapping, `Messages` key parity) plus the existing capture tools. | add the `fr`/`en` key-parity test in the same wave as the bundles, not later. |
| Craftsmanship | 4 | Smallest correct change per wave; no new dependency; `Messages` grows screen by screen instead of a big-bang string extraction. | none |
| Decision record | 5 | Six ADR-style blocks with forces, ≥2 credible options, consequences and reversibility; the riskiest one (D3, dropping the stored password) names its cost. | none |

## Concerns found and folded in

1. **AC8 could be read as an authorization claim.** Hiding drawer actions is
   presentation; the API stays `IsAuthenticated`. `SPEC.md` → D1 consequences
   and Scope (out) now say so, and `VALIDATION.md` will repeat it.
2. **D3 has no answer for a dead refresh token.** Added D5: one
   `SessionExpired` callback returns to the login scene instead of a silent
   credential re-login or a modal per screen.
3. **AC5 could overclaim translation.** `Messages` covers login, profile and
   shell only; the record screens are US7, and the AC names the externalized
   surfaces rather than "the app".

## Verdict

```
VERDICT: ACCEPT
STEELMAN: One identity endpoint derived from Django's own account flag, one
login scene, one profile tab and one preference store give the app a real
notion of "who is using it" without a migration, a new dependency or a
second stylesheet — and every choice is reversible behind a single file.
SMALLEST NEXT STEP: keep the drawer gating explicitly presentational in the
spec and PR body (done) so no reviewer mistakes it for server authorization.
REVISIT TRIGGER: the first request for a role beyond admin/operator, or for
preferences that follow the user across machines — either reopens D1/D4.
```
