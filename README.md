# Gest_Comm_ClientFX

A JavaFX desktop client for a *gestion commerciale* (small-business sales
management) application: articles, customers, quotes and invoices, with a
French UI.

The window is wired to the [PyGestComm](../PyGestComm) Django REST backend:
the Application menu opens a settings screen (server URL + credentials,
stored in the OS preferences store), the Client/Article/Devis menus list
live data from the API in dedicated tabs, *Nouveau client* / *Nouveau
article* open create forms that POST to the API, and *Nouveau devis*
opens a quote form with a client picker, article-driven line items and
live totals that POSTs nested `lignes` to `/api/devis`. The Facture menu
adds a matching invoice form (optionally prefilled from a devis) and a
*Convertir en facture* action on the Devis tab that calls
`POST /api/devis/<id>/convertir`. Bons de livraison, edit (PUT) and
delete are wired too; a refused action is explained in French with the
backend's own reason (e.g. *Suppression impossible : this client cannot
be deleted…*), see `controllers/ApiMessages`.

## Run it

Requirements: JDK 17 (JavaFX 17 is pulled from Maven, so no separate SDK is
needed) and a running PyGestComm backend:

```bash
# backend (PyGestComm repo)
cd src/GestCommProject && python manage.py runserver   # http://localhost:8000

# client (this repo)
mvn javafx:run
```

On first launch open **Application → Parametre de l'application**, enter the
server URL (default `http://localhost:8000`) and the API username/password
(a Django user — `python manage.py createsuperuser` on the backend), then
**Tester la connexion**. The app exchanges credentials once for JWT
tokens (`POST /api/token`) and sends `Authorization: Bearer <access>`;
the rotating refresh token is persisted so restarts skip login. Document
codes are optional — leave them blank and the backend auto-numbers
(DEV-/FAC-/BL- sequences).
**Application → Se déconnecter** revokes the refresh token server-side
(`/api/token/logout`) and clears local tokens.
request.

To build without starting the app:

```bash
mvn package
# target/Gest_Comm_ClientFX-1.0-SNAPSHOT.jar
```

The app opens a 1200×700 window titled *Gestion Commerciale*, loaded from
`views/HomeScreen.fxml`.

## What is in the window

- **Application** menu — *Parametre de l'application* opens the settings
  screen (wired)
- **Article** menu — *Nouveau article* opens a create form (wired);
  *Liste des articles* opens the Articles tab (wired)
- **Client** menu — *Nouveau client* / *Modifier* / *Supprimer* (wired);
  *Liste des clients* opens the Clients tab (wired); *Nouveau Bon Liv* /
  *Liste des Bon Livs* open the delivery-note form/tab (wired)
- **Help** menu — *Assistant* opens the assistant dialog (wired)
- **Devis** menu — *Nouveau devis* opens the quote form (wired, with
  line items + totals); *Liste des devis* opens the Devis tab (wired);
  *Convertir en facture* converts the selected `valide` devis
- **Facture** menu — *Nouvelle facture* opens the invoice form (wired,
  optional devis prefill); *Liste des factures* opens the Factures tab
  (wired)
- tabs: *Bienvenue*, *Clients*, *Articles*, *Devis*, *Factures* and
  *Bons de livraison* — each list tab has action buttons and a status line

## Layout

```
src/main/java/com/xdev/gest_comm_clientfx/
  app/StartApp.java          Application subclass, the entry point
  app/HomeScreen.java        loads views/HomeScreen.fxml
  app/ConfigScreen.java      loads views/ConfigScreen.fxml
  api/ApiClient.java         HTTP/JSON client (java.net.http + Jackson,
                             JWT Bearer auth)
  api/ApiConfig.java         server URL + credentials + refresh token
                             (Java Preferences)
  api/ApiException.java      transport/HTTP error type
  controllers/               @FXML controllers — HomeScreenCtrl,
                             ConfigScreenCtrl, NouveauClientCtrl,
                             NouvelArticleCtrl, NouveauDevisCtrl,
                             NouvelFactureCtrl all wired
  models/Article.java        snake_case fields + @JsonProperty bridge to
                             the API's camelCase JSON
  models/Client.java         mirrors the backend Client fields 1:1
  models/Devis.java          quote header + nested LigneDevis + totals
  models/LigneDevis.java     devis line item (article or free text)
  models/Facture.java        invoice header + nested LigneFacture + totals
  models/LigneFacture.java   facture line item (article or free text)
  models/BonLiv.java         delivery note header + nested LigneBonLiv
  models/LigneBonLiv.java    bonliv line item (article or free text)
  views/*.fxml               the layouts, copied onto the classpath
```

The `.fxml` files live under `src/main/java` and are copied to the classpath
by the `maven-resources-plugin` resource entry in `pom.xml`, which is why
`HomeScreen` can load `/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml`.

## Status

| Piece | State |
|---|---|
| Window, menus, tabs | laid out in FXML |
| Menu actions | everything wired — all five resources list/create/edit/delete/statut, conversion, assistant |
| `HomeScreenCtrl`, `ConfigScreenCtrl` | wired (`@FXML` injection, background loads) |
| `NouveauClientCtrl`, `NouvelArticleCtrl` | wired — POST forms with client-side validation |
| `NouveauDevisCtrl` | wired — client/article pickers, line editor, live totals, POSTs nested `lignes` |
| `NouvelFactureCtrl` | wired — same line editor + optional devis prefill |
| `Article`, `Client`, `Devis`, `LigneDevis`, `Facture`, `LigneFacture`, `BonLiv`, `LigneBonLiv` | full accessors + Jackson JSON mapping (tested) |
| Row actions | edit (PUT), delete (DELETE + confirm), statut PATCH on devis/factures |
| Assistant | Help → Assistant dialog over `POST /api/assistant/ask` |
| HTTP client | `api/ApiClient` — list/post/put/delete/patch + `convertDevis` + `askAssistant` (all routes) |
| Tests | 29 JUnit 5 tests (model accessors, JSON mapping, API error parsing + French phrasing, home-screen layout, null-safe screen formatting, tab workspace policy, theme stylesheet) |
| Navigation | Panels open as tabs (close ×, pin ★); only Bienvenue opens at startup; the right-hand drawer carries the active tab's actions and collapses to a rail instead of hiding |
| Look and feel | Industrial theme in `views/industrial.css` (graphite surfaces, steel borders, one amber accent), applied once by `ui/Theme` — forms, lists and dialogs share it; window minimum 1024×640, form tabs scroll rather than clip, JavaFX's own strings follow the French locale |

## Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) builds with JDK 17 and
`mvn -B package` on every push and pull request to `master`.

## License

MIT — see [LICENSE](LICENSE).
