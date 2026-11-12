# Gest_Comm_ClientFX

A JavaFX desktop client for a *gestion commerciale* (small-business sales
management) application: articles, customers, delivery notes and customer
invoices, with a French UI.

This is the client skeleton. The window, its menus and the domain model exist;
the controllers and the screens behind the menus do not yet, so the menus open
nothing and no data is loaded. The repository is kept as the starting point for
the JavaFX front end of the [Gest_Comm](../Gest_Comm) back end.

## Run it

Requirements: JDK 17 (JavaFX 17 is pulled from Maven, so no separate SDK is
needed).

```bash
mvn javafx:run
```

`javafx:run` puts the JavaFX modules on the module path for you. To build
without starting the app:

```bash
mvn package
# target/Gest_Comm_ClientFX-1.0-SNAPSHOT.jar
```

The app opens a 1200×700 window titled *Gestion Commerciale*, loaded from
`views/HomeScreen.fxml`.

## What is in the window

- **Application** menu — application settings, sequence settings, and the
  template's Save As / Open / Close items (inert)
- **Article** menu — new article, article list (inert)
- **Client** menu — new customer, customer list (inert)
- **Bon Liv** menu — new delivery note, delivery-note list (inert)
- **Facture** menu — new invoice, invoice list (inert)
- a search field and a single *Bienvenue* tab

## Layout

```
src/main/java/com/xdev/gest_comm_clientfx/
  app/StartApp.java          Application subclass, the entry point
  app/HomeScreen.java        loads views/HomeScreen.fxml
  app/ConfigScreen.java      empty placeholder
  controllers/               empty FXML controllers
  models/Article.java        article: reference, description, purchase and
                             sale price, discount and VAT on both sides
  models/Client.java         empty
  models/BonLivClient.java   empty (delivery note)
  models/FactureClient.java  empty (invoice)
  views/*.fxml               the layouts, copied onto the classpath
```

The `.fxml` files live under `src/main/java` and are copied to the classpath
by the `maven-resources-plugin` resource entry in `pom.xml`, which is why
`HomeScreen` can load `/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml`.

## Status

| Piece | State |
|---|---|
| Window, menus, tab | laid out in FXML |
| Menu actions | not wired — no `onAction` handlers |
| `HomeScreenCtrl`, `ConfigScreenCtrl` | empty classes, no `@FXML` fields |
| `Article` | fields only, no getters/setters |
| `Client`, `BonLivClient`, `FactureClient` | empty classes |
| Persistence / HTTP client | none |

## Continuous integration

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) builds with JDK 17 and
`mvn -B package` on every push and pull request to `master`. There are no
tests yet.

## License

MIT — see [LICENSE](LICENSE).
