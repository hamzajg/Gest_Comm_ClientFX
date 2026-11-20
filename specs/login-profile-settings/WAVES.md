# Waves — login-profile-settings

Each wave IS an iteration: a time-boxed, independently shippable
increment — one branch, one PR, green tests, demoable outcome.
Do not start wave N+1 until wave N merges.

## Warnings

- US4 lands in wave 3 but its scenarios are tagged @w2 — retag EXAMPLES.feature to @w3 or adjust Depends
- US9 lands in wave 2 but its scenarios are tagged @w1 — retag EXAMPLES.feature to @w2 or adjust Depends

## Wave 1 — iteration 1

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US1 | As an office operator, I want the app to ask who I am before opening the workspace, so that a shared workstation does not hand the commercial data to whoever launches it | M | — | `LoginView` scene, `/api/token` exchange, `/api/me` identity, stage swap in `StartApp`, error line on refusal. |
| US2 | As a developer, I want identity and role served by the backend, so that the client never guesses who the token belongs to | S | — | `GET /api/me` deriving `role` from `is_staff`; tests for both roles and anonymous. |

## Wave 2 — iteration 2

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US3 | As an office operator, I want a profile screen that shows my account and role, so that I know which access I am using | M | US1, US2 | profile tab in the workspace (identity, e-mail, role badge, sign out). |
| US9 | As a security reviewer, I want no password on disk, so that a stolen laptop does not hand over the commercial database | S | US1 | `ApiConfig` persists username + refresh token only; `ConfigScreen` keeps the server URL only; test asserts the password key is gone. |

## Wave 3 — iteration 3

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US4 | As an office operator, I want to change my own password, so that I do not need IT for a routine rotation | M | US3 | `POST /api/me/password`, current + new password, Django validators, French refusal messages. |
| US5 | As an office operator, I want to choose a dark or light theme, so that the workspace matches the light in my office | S | US3 | `Theme.Skin` selector in the profile tab, persisted in Java Preferences, applied live. |
| US6 | As an office operator, I want the interface in French or English, so that a colleague who does not read French can use the app | L | US3 | `Messages` bundles for login/profile/shell/drawer, scene rebuild on change, `fr`/`en` key parity test. |

## Wave 4 — iteration 4

| Item | Title | Size | Deps | Notes |
|------|-------|------|------|-------|
| US7 | As a bilingual operator, I want the record tables and forms translated too, so that the whole app follows the chosen language | L | US6 | migrate the eight FXML forms and the table headers into `Messages`; the gap US6 documents. |
| US8 | As an administrator, I want destructive actions limited to administrators, so that a regular operator cannot delete or validate records | M | US2 | drawer hides delete/validate/refuse/convert for `operator`; admin keeps them. |
