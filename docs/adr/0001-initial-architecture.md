# ADR 0001 — Initial architecture

Date: 2026-11-13 (baseline recorded retroactively)
Status: accepted (describes what is built)

## Context

The client half of the *gestion commerciale* system: a JavaFX desktop app
for articles, customers, delivery notes, and invoices, backed by the
Gest_Comm ADF REST service.

## Decisions

1. **JavaFX + FXML, Maven-built.** `javafx-controls`/`javafx-fxml` 17.0.2
   from Maven Central; `mvn javafx:run` for dev. Rationale: standard
   desktop-Java stack, zero-install JavaFX via the plugin's module path.
2. **Loader-wrapper screens, stub controllers.** `HomeScreen`/
   `ConfigScreen` load FXML and expose roots; controllers are empty
   placeholders. Rationale: layout-first development — the window exists
   before behaviour does.
3. **Models as field-only shells.** `Article` carries the full field set
   (prices, discounts, VAT, last-sale price) with no getters; other
   domain models are empty classes. Rationale: capture the domain shape
   early; behaviour arrives with the service layer.
4. **No HTTP/persistence yet.** Deliberate — the skeleton proves the UI
   shell first. Rationale: KISS; don't build a client for an API contract
   (ADF-generated, unstable) that isn't exercised yet.
5. **French domain vocabulary in code.** Field names like
   `prix_vente_unitaire`, `remise_achat`, `tva_*` mirror the business
   language and the backend schema. Rationale: consistency across the
   two-repo system beats anglicising.
6. **CI = Maven package only.** JDK 17, `mvn -B package`, no tests. The
   build proves compilation; nothing else is claimed.

## Consequences

- "Runs" currently means "shows a window" — every menu action is inert.
- When the HTTP layer lands it must target Gest_Comm's ADF-generated
  shapes; pin that contract in an ADR before writing the client calls.
- Models will need getters/setters (or records) once binding/JSON mapping
  arrives — an intentional later step, not an oversight.
