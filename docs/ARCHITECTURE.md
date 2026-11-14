# Architecture — Gest_Comm_ClientFX

Current-state map. Descriptive, not aspirational; see `docs/adr/` for why.

## System shape

```
StartApp (JavaFX Application)
   └─ HomeScreen ──loads──► views/HomeScreen.fxml
        │                      menus: Application / Article / Client /
        │                      Bon Liv / Facture + search + Bienvenue tab
        ▼
controllers/  HomeScreenCtrl, ConfigScreenCtrl — EMPTY stubs (no @FXML)
models/       Article + Client POJOs with accessors (Client mirrors the
              PyGestComm backend fields); BonLivClient, FactureClient
              still empty shells
        │
        ▼ (intended, not built)
Gest_Comm ADF backend  /rest/*  (Article view object only)
```

## Design

- **FXML + loader pattern.** `HomeScreen`/`ConfigScreen` wrap
  `FXMLLoader.load(...)` and expose `getRootView()`; `StartApp` puts the
  root in a `Scene`. Controllers exist as placeholders for `@FXML`
  injection but aren't wired.
- **Anemic models.** `Article` declares the domain fields
  (`id_article`, `ref_article`, `desc_article`, purchase/sale prices,
  `remise_*`, `tva_*`, `dernier_prix_vente_unitaire`) with no behaviour —
  a DTO-shaped shell matching the future REST payloads.
- **No service layer.** No HTTP client, no repository, no persistence —
  the app is currently a static window.

## State

None. No local storage, no network calls. All state will come from the
Gest_Comm backend once the client layer is built.

## Contract status

Unspecified. The backend (`Gest_Comm`) exposes ADF-generated REST for a
single `Article` view object; the client's model fields anticipate the
article payload but nothing calls it yet. First HTTP implementation should
pin the contract in an ADR here and in `Gest_Comm`.
