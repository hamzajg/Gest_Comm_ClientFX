# Spec: login-profile-settings

> Spec-driven development artifact. This file is the contract: implementation
> is done when every acceptance criterion maps to a scenario in
> `EXAMPLES.feature` and every scenario has a passing check.

## Problem

Gest_Comm_ClientFX never asks who is using it. `ApiConfig` stores the
username *and the password* in Java Preferences and `ApiClient` silently
exchanges them for a JWT, so the app opens straight into the workspace: on a
shared office workstation anyone who launches it is inside the commercial
data, and nobody can tell whether they are the administrator or a regular
operator. The only settings surface (`ConfigScreen`) is a connection form —
server URL, username, password — with no profile, no role and no preference.

Two capabilities landed since that form was written and have no way to be
chosen: the theme now has a dark and a light skin (`Theme.Skin`), and the UI
is French-only with the locale forced in `StartApp.main`. The backend also
exposes no identity endpoint at all: `/api/*` authenticates with JWT but
nothing returns *who* the token belongs to, so role cannot be displayed, let
alone enforced.

## Users

- Primary: the *gestionnaire commercial* (office operator) on a shared
  workstation, who must sign in as themselves, pick a comfortable theme and
  language once, and change their own password without calling IT.
- Secondary: the administrator (`is_staff`) who owns accounts in Django admin
  and needs the destructive actions to belong to them.
- Tertiary: the developer/reviewer checking the login, profile and role paths
  in CI and captures.

## Outcome hypothesis

- Expected user outcome: launching the app asks who you are, the workspace
  says which account and role you are in, and the look/language/password are
  yours and survive a restart.
- Opportunity evidence: assumption — requested directly by the product owner
  after the tab-workspace and theme iterations; the desktop app has no
  telemetry.
- Signal: the demo script in `VALIDATION.md` runs end to end (sign in as
  operator, switch theme and language, change password, sign out, sign in with
  the new password, sign in as admin and see the admin-only actions); both
  suites green; no regression in the existing 30 client tests and 56 backend
  tests.
- Follow-up owner/date: product owner at the next iteration review.

## Scope (in)

- Backend (`PyGestComm`): `GET /api/me` (identity + derived role),
  `PATCH /api/me` (first name, last name, e-mail), `POST /api/me/password`
  (current + new password, Django password validators), plus tests. No
  migration, no custom user model.
- Client: a login scene that gates the workspace; a profile tab (identity,
  role, theme, language, password change, sign out); session state that
  knows the signed-in `UserProfile`; preferences persisted locally; the
  existing connection settings screen reduced to the server URL only.
- String externalization for the login, profile, shell menus and drawer
  captions into `Messages` bundles (`fr`, `en`); language choice applies to
  those surfaces immediately, by rebuilding the workspace scene.
- The five record tables (column headers, list status, selection and
  confirmation messages) and the eight record forms (client, article, quote,
  invoice, delivery note, each in create and edit) externalized through the
  same bundles, so the whole app follows the language (US7).
- Role enforcement for the destructive drawer actions (delete, validate,
  refuse, convert) — admin only — with the backend's own permission unchanged.
- `tools/SkinShots.java` and `tools/ThemeStates.java` extended to cover the
  login and profile surfaces; spec artifacts in this directory.

## Scope (out)

- Custom Django user model, per-user server-side preferences, user
  administration UI (accounts stay in Django admin), password reset by
  e-mail, 2FA, SSO, session-timeout policy, audit logging.
- Server-side role permissions on `/api/*`: the drawer gating in US8 is
  presentation. The API keeps `IsAuthenticated`; an operator token can still
  call a destructive endpoint directly. That gap is named in VALIDATION.md
  and is a follow-up feature, not something this one claims to close.
- Translating the *data*: statut values (`brouillon`, `valide`, `refuse`,
  `livre`, `payee`, `annulee`) are API enum values, not labels, and are shown
  as sent. Only the surrounding screens are translated.
- Window chrome, animations, third-party theme libraries.

## Acceptance criteria

| # | Criterion | Scenario(s) |
|---|-----------|-------------|
| 1 | The app opens on a login screen; the workspace appears only after credentials are accepted and identity is loaded | Sign in with valid credentials |
| 2 | Wrong credentials keep the user on the login screen with the backend's message and no workspace | Refuse wrong credentials |
| 3 | The profile tab shows the signed-in user's name, e-mail and role, derived server-side | Profile shows identity and role |
| 4 | Theme choice applies immediately and survives a restart | Theme survives a restart |
| 5 | Language choice switches the externalized strings (login, profile, shell) and survives a restart | Language switches the shell |
| 6 | A user can change their own password with the current one; a wrong current password is refused; the new password signs in | Change password |
| 7 | Signing out clears the session, blacklists the refresh token and returns to the login screen | Sign out returns to login |
| 8 | Regular operators do not get the admin-only actions; administrators do | Role gates destructive actions |
| 9 | No password is persisted on disk after this feature | No stored password |
| 10 | Both suites build and pass | (quality gate G2) |
| 11 | The five record tables and the record forms follow the chosen language | All record screens follow the language |

## Decisions

### D1 — Identity is Django's `User`, exposed by a dedicated `/api/me`

- Context & forces: accounts already live in Django admin (`createsuperuser`,
  `/api/token`); the client has no way to learn who the token belongs to. A
  custom user model would be the "proper" home for a role field but costs a
  migration on an existing SQLite database plus an `AUTH_USER_MODEL` switch
  that every future migration inherits.
- Options: (a) custom `User` with a `role` column; (b) Django `Group`s with a
  `role` group; (c) derive the role from the existing `is_staff` flag and
  serve it from `/api/me`.
- Decision & why: (c). `is_staff` already means "may administer", it is what
  `/admin` gates on, and it needs no migration, so accounts created before
  this feature keep working. The role is computed server-side per request and
  is never taken from the client.
- Consequences: (+) zero-migration, admin stays the single account source;
  (−) two roles only (`admin`/`operator`), and a future third role means
  revisiting this decision; (−) hiding actions in the drawer is presentation
  only — the API keeps `IsAuthenticated`, so an operator can still call
  `DELETE /api/client/<id>` directly. Server-side role permissions on the
  API are out of scope for this feature and must not be claimed as done.
- Reversibility: two-way door behind `serializers.UserProfileSerializer`.
- Revisit trigger: the first request for a role beyond administrator/operator.

### D2 — The login gate is a scene swap, not a modal over the workspace

- Context & forces: `StartApp` builds one `Scene` and `Theme` is installed per
  scene. A modal dialog over an already-built workspace leaks the data behind
  it (and keyboard focus reaches it); a second window doubles the theme and
  window-management work.
- Options: (a) blocking modal dialog over `HomeScreen`; (b) separate login
  `Stage`; (c) build the workspace only after login (`LoginScene` → workspace
  scene on the same `Stage`).
- Decision & why: (c). No workspace exists until identity is known, so there
  is nothing to leak; one window, one theme install per scene, and the same
  stage is reused for the sign-out return.
- Consequences: (+) clean state, no focus leaks; (−) two scenes to keep
  skinned and sized, and sign-out rebuilds the workspace scene rather than
  just closing a dialog.
- Reversibility: two-way door — the login scene is a plain FXML view.

### D3 — Credentials stop being persisted; only the refresh token is kept

- Context & forces: `ApiConfig.save()` writes username *and* password into
  Java Preferences (plaintext, OS-readable by the same user). The refresh
  token already survives restarts and is rotated/blacklisted by SimpleJWT, so
  the password only exists to bootstrap the first token.
- Options: (a) keep the password so the client can re-login silently when the
  refresh token dies; (b) persist username + refresh token, keep the password
  in memory for the session only, and ask again when the session expires;
  (c) persist nothing and require a login on every launch.
- Decision & why: (b). It removes the plaintext secret from disk while keeping
  the seven-day silent session; when rotation fails the user is asked to sign
  in instead of the app quietly reusing a stored password.
- Consequences: (+) no stored password, logout is meaningful; (−) one extra
  sign-in after a long absence, and `ConfigScreen` must stop showing/saving a
  password field.
- Reversibility: two-way door (the field can be re-added) but re-adding it is
  a regression; treated as one-way in review.

### D4 — Look and language preferences are local to the workstation

- Context & forces: theme and language describe how *this* machine shows the
  app; the backend has no preference table and adding one means a migration,
  a serializer and a write path for a non-domain concern. The operator may
  work on more than one PC, but rarely.
- Options: (a) `PATCH /api/me/preferences` on the server; (b) Java
  Preferences on the workstation; (c) both, server as the source of truth.
- Decision & why: (b). No schema change, no new endpoint, instant apply, and
  the choice survives restarts (the requirement). Server sync is a later
  feature if it is ever asked for.
- Consequences: (+) smallest change, no migration; (−) preferences do not
  follow the user across machines — documented in `VALIDATION.md`.
- Reversibility: two-way door (add a preferences endpoint later and read it at
  login).

### D5 — A dead session returns to the login scene, it does not retry silently

- Context & forces: D3 removes the stored password, so `ApiClient` can no
  longer bootstrap a token after the refresh token expires or is revoked.
  Today `refreshTokens()` falls back to `obtainTokens()` with the saved
  credentials and a 401 surfaces as an exception on whatever screen was
  loading.
- Options: (a) keep the password in memory only and prompt with a modal on
  401; (b) let the call fail and have each screen report it; (c) one
  `SessionExpired` callback on `ApiClient` that swaps the scene back to login
  with the username prefilled.
- Decision & why: (c). Credentials are only ever entered on the login scene,
  so there is exactly one place that can mint a session; every 401 path ends
  there instead of leaving a half-loaded screen behind.
- Consequences: (+) one auth path, no modal stack; (−) an in-flight action is
  lost when the session dies, and the user sees the login screen mid-task —
  acceptable for a 7-day refresh window.
- Reversibility: two-way door.

### D6 — Language switches by rebuilding the workspace scene

- Context & forces: labels are baked into FXML and Java string literals;
  JavaFX does not re-localize a loaded scene, and forcing the locale only in
  `main` means a change would otherwise need an app restart.
- Options: (a) restart the app on language change; (b) bind every label to a
  property (invasive, touches every controller); (c) externalize the login,
  profile, shell and drawer strings into `Messages` bundles and rebuild the
  scene on change, keeping the session.
- Decision & why: (c). The rebuild is one call (`StartApp.showWorkspace()`),
  the session survives, and strings move into bundles incrementally; screens
  not yet externalized stay French and are tracked by US7 rather than being
  claimed as translated.
- Consequences: (+) immediate switch, honest scope, no restart; (−) the
  workspace scene is rebuilt (open tabs are lost — acceptable and stated), and
  the bundles must be kept in sync between `fr` and `en` (guarded by a test).
- Reversibility: two-way door — bundles can grow screen by screen.
