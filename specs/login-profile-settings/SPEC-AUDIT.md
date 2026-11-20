# Spec audit — login-profile-settings

Phase: `spec`. Checked against `Gest_Comm_ClientFX` and `PyGestComm` at
`master` (client `ab68e82`, backend unchanged by this feature yet).

## Grounding checks

| Claim in SPEC.md | Verified? | Evidence |
|---|---|---|
| `ApiConfig` persists username **and password** in Preferences | yes | `src/main/java/.../api/ApiConfig.java` — `save()` writes `baseUrl`, `username`, `password`, `refreshToken`. |
| The client can exchange stored credentials silently | yes | `api/ApiClient.java` — `ensureAccessToken()` → `refreshTokens()` → `obtainTokens()` posting `/api/token`. |
| The app opens the workspace with no identity step | yes | `app/StartApp.java` builds `new Scene(new HomeScreen().getRootView(), …)` directly, no gate. |
| The only settings surface is a connection form with a password field | yes | `controllers/ConfigScreenCtrl.java` + `views/ConfigScreen.fxml` (`baseUrlField`, `usernameField`, `passwordField`). |
| No backend identity endpoint exists | yes | `GestCommProject/urls.py` lists `/api/client…`, `/api/devis…`, `/api/token…`, `/admin/`; no `/api/me`. |
| JWT auth with `IsAuthenticated` on `/api/*` | yes | `settings.py` `REST_FRAMEWORK` blocks + `urls.py` token routes. |
| Two skins exist and can be selected | yes | `ui/Theme.java` `Skin` enum + `.root.light` block in `views/industrial.css`. |
| The UI is French-only, locale forced at startup | yes | `app/StartApp.java` `Locale.setDefault(Locale.FRANCE)` in `main`. |
| Suite sizes quoted in the signal (30 client / 56 backend) | yes | `mvn -B package` → 30 tests BUILD SUCCESS (this session); `tests.py` defines 56 `test_` methods. |

## Acceptance map

| AC | Scenario exists | Wave | Check planned |
|----|-----------------|------|---------------|
| 1 | Sign in with valid credentials | 1 | backend token test + login capture |
| 2 | Refuse wrong credentials | 1 | login error capture + `ApiMessages` test |
| 3 | Profile shows identity and role | 2 | profile capture + serializer test |
| 4 | Theme survives a restart | 3 | preferences test + light capture |
| 5 | Language switches the shell | 3 | `Messages` parity test + English capture |
| 6 | Change password | 2 | `POST /api/me/password` tests (ok / wrong / weak) |
| 7 | Sign out returns to login | 2 | logout test (refresh blacklisted) + capture |
| 8 | Role gates destructive actions | 4 | controller test on the drawer action list |
| 9 | No stored password | 1 | `ApiConfigTest` asserting the key is not written |
| 10 | Both suites build and pass | — | G2 + per-wave evidence |

## Findings

- No blocker. Every repo claim in the spec was opened and confirmed; the two
  figures quoted (30/56 tests) match the suites as they run today.
- Scope boundary corrected during review: AC8 is presentation-only (D1
  consequences, Scope out) and AC5 names the externalized surfaces instead of
  claiming the whole app is translated. Both are stated honestly rather than
  overclaimed.

```
AUDIT: PASS
PHASE: spec
BLOCKERS: 0
CONCERNS: 0
UNVERIFIED: none — every citation was read in the repository; the runtime
  behaviour of a real light-skin session on a physical display remains
  unverified but is not claimed by this spec.
EVIDENCE: read api/ApiConfig.java, api/ApiClient.java, app/StartApp.java,
  controllers/ConfigScreenCtrl.java, ui/Theme.java, views/industrial.css,
  PyGestComm GestCommProject/urls.py + settings.py, GestCommApp/tests.py;
  ran `mvn -B package` (30 tests, BUILD SUCCESS) and `grep -c "def test"`
  on tests.py (56).
```
