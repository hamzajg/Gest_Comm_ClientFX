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
`POST /api/devis/<id>/convertir`. Bon Liv and edit/delete are still
stubs.

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
**Tester la connexion**. Credentials are sent as HTTP Basic auth on every
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
  screen (wired); the template's sequence/Save/Open/Close items stay inert
- **Article** menu — *Nouveau article* opens a create form (wired);
  *Liste des articles* opens the Articles tab (wired)
- **Client** menu — *Nouveau client* opens a create form (wired);
  *Liste des clients* opens the Clients tab (wired); *Bon Liv* and
  *Facture* items are still inert
- **Help** menu — unchanged
- **Devis** menu — *Nouveau devis* opens the quote form (wired, with
  line items + totals); *Liste des devis* opens the Devis tab (wired);
  *Convertir en facture* converts the selected `valide` devis
- **Facture** menu — *Nouvelle facture* opens the invoice form (wired,
  optional devis prefill); *Liste des factures* opens the Factures tab
  (wired)
- tabs: *Bienvenue*, *Clients*, *Articles*, *Devis* and *Factures* —
  each list tab has an *Actualiser* button and a status line

## Layout

```
src/main/java/com/xdev/gest_comm_clientfx/
  app/StartApp.java          Application subclass, the entry point
  app/HomeScreen.java        loads views/HomeScreen.fxml
  app/ConfigScreen.java      loads views/ConfigScreen.fxml
  api/ApiClient.java         HTTP/JSON client (java.net.http + Jackson,
                             Basic auth on every request)
  api/ApiConfig.java         server URL + credentials (Java Preferences)
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
  models/BonLivClient.java   empty (delivery note)
  views/*.fxml               the layouts, copied onto the classpath
```

The `.fxml` files live under `src/main/java` and are copied to the classpath
by the `maven-resources-plugin` resource entry in `pom.xml`, which is why
`HomeScreen` can load `/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml`.

## Status

| Piece | State |
|---|---|
| Window, menus, tabs | laid out in FXML |
| Menu actions | config + client/article/devis/facture lists, create forms and devis→facture conversion wired; Bon Liv + edit/delete inert |
| `HomeScreenCtrl`, `ConfigScreenCtrl` | wired (`@FXML` injection, background loads) |
| `NouveauClientCtrl`, `NouvelArticleCtrl` | wired — POST forms with client-side validation |
| `NouveauDevisCtrl` | wired — client/article pickers, line editor, live totals, POSTs nested `lignes` |
| `NouvelFactureCtrl` | wired — same line editor + optional devis prefill |
| `Article`, `Client`, `Devis`, `LigneDevis`, `Facture`, `LigneFacture` | full accessors + Jackson JSON mapping (tested) |
| `BonLivClient` | empty class (delivery note) |
| HTTP client | `api/ApiClient` — list + post + `convertDevis` (all document routes) |
| Tests | 7 JUnit 5 tests (model accessors + JSON mapping) |

## Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) builds with JDK 17 and
`mvn -B package` on every push and pull request to `master`.

## License

MIT — see [LICENSE](LICENSE).
