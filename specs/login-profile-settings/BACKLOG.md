# Backlog: login-profile-settings

Items are USER STORIES in the parseable format below — keep the heading
shape `## US<n> — <story>` and the field lines intact so `wave_plan` can
parse them. Write the story as: "As a <role>, I want <goal>, so that
<value>" — the title after the dash IS the story.

## US1 — As an office operator, I want the app to ask who I am before opening the workspace, so that a shared workstation does not hand the commercial data to whoever launches it
- Scenarios: @w1
- Size: M
- Notes: `LoginView` scene, `/api/token` exchange, `/api/me` identity, stage swap in `StartApp`, error line on refusal.

## US2 — As a developer, I want identity and role served by the backend, so that the client never guesses who the token belongs to
- Scenarios: @w1
- Size: S
- Notes: `GET /api/me` deriving `role` from `is_staff`; tests for both roles and anonymous.

## US3 — As an office operator, I want a profile screen that shows my account and role, so that I know which access I am using
- Scenarios: @w2
- Depends: US1, US2
- Size: M
- Notes: profile tab in the workspace (identity, e-mail, role badge, sign out).

## US4 — As an office operator, I want to change my own password, so that I do not need IT for a routine rotation
- Scenarios: @w2
- Depends: US3
- Size: M
- Notes: `POST /api/me/password`, current + new password, Django validators, French refusal messages.

## US5 — As an office operator, I want to choose a dark or light theme, so that the workspace matches the light in my office
- Scenarios: @w3
- Depends: US3
- Size: S
- Notes: `Theme.Skin` selector in the profile tab, persisted in Java Preferences, applied live.

## US6 — As an office operator, I want the interface in French or English, so that a colleague who does not read French can use the app
- Scenarios: @w3
- Depends: US3
- Size: L
- Notes: `Messages` bundles for login/profile/shell/drawer, scene rebuild on change, `fr`/`en` key parity test.

## US7 — As a bilingual operator, I want the record tables and forms translated too, so that the whole app follows the chosen language
- Scenarios: @w4
- Depends: US6
- Size: L
- Notes: migrate the eight FXML forms and the table headers into `Messages`; the gap US6 documents.

## US8 — As an administrator, I want destructive actions limited to administrators, so that a regular operator cannot delete or validate records
- Scenarios: @w4
- Depends: US2
- Size: M
- Notes: drawer hides delete/validate/refuse/convert for `operator`; admin keeps them.

## US9 — As a security reviewer, I want no password on disk, so that a stolen laptop does not hand over the commercial database
- Scenarios: @w1
- Depends: US1
- Size: S
- Notes: `ApiConfig` persists username + refresh token only; `ConfigScreen` keeps the server URL only; test asserts the password key is gone.
