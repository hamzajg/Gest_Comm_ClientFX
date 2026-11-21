# Delivery Loop — login-profile-settings

Status: running
Iterations: 0
Updated: 2026-11-21T12:44:19Z

| Wave | Status | Branch | PR | Evidence | Updated |
|------|--------|--------|----|----------|---------|
| 1 | done | feat/login-profile-settings-w1 | #12 | login gate + /api/me; client mvn package 36/36, backend 69/69; CI green on both PR #12s; evidence bundle verified (2 ste | 2026-11-20T17:30:34Z |
| 2 | done | feat/login-profile-settings-w2 | #13 | profile tab (/api/me identity, e-mail, role badge), sign-out to the login gate, password no longer persisted, settings r | 2026-11-21T12:09:47Z |
| 3 | done | feat/login-profile-settings-w3 | #14 | theme + language preferences (fr/en bundles, shell rebuild), password change form, login/shell/drawer externalized; clie | 2026-11-21T12:44:19Z |
| 4 | pending | — | — | — | — |

## Gates

| Gate | Status | Evidence | Updated |
|------|--------|----------|---------|
| G1 | met | file:specs/login-profile-settings/DECISION-REVIEW.md — verdict ACCEPT (7 lenses; 3 concerns folded into SPEC) | 2026-11-20T17:11:35Z |
| G2 | met | backend: python manage.py test -> Ran 56 tests OK; client: mvn -B package -> 30 tests BUILD SUCCESS | 2026-11-20T17:12:34Z |

## Events

- 2026-11-20T17:11:35Z gate G1 met: file:specs/login-profile-settings/DECISION-REVIEW.md — verdict ACCEPT (7 lenses; 3 concerns folded into SPEC)
- 2026-11-20T17:12:34Z gate G2 met: backend: python manage.py test -> Ran 56 tests OK; client: mvn -B package -> 30 tests BUILD SUCCESS
- 2026-11-20T17:12:34Z start wave 1 on feat/login-profile-settings-w1
- 2026-11-20T17:30:34Z complete wave 1 (#12): login gate + /api/me; client mvn package 36/36, backend 69/69; CI green on both PR #12s; evidence bundle verified (2 ste
- 2026-11-21T11:46:42Z start wave 2 on feat/login-profile-settings-w2
- 2026-11-21T12:09:47Z complete wave 2 (#13): profile tab (/api/me identity, e-mail, role badge), sign-out to the login gate, password no longer persisted, settings r
- 2026-11-21T12:19:09Z start wave 3 on feat/login-profile-settings-w3
- 2026-11-21T12:44:19Z complete wave 3 (#14): theme + language preferences (fr/en bundles, shell rebuild), password change form, login/shell/drawer externalized; clie
