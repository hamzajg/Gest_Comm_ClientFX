package com.xdev.gest_comm_clientfx.ui;

import javafx.collections.ObservableList;
import javafx.scene.Scene;

import java.net.URL;

/**
 * The application's industrial stylesheet.
 *
 * The whole UI lives in one {@link Scene}, so installing the sheet once there
 * themes every tab-hosted screen (lists, forms, settings, assistant). Colours
 * live only in {@code views/industrial.css} — see specs/industrial-look-feel.
 *
 * The sheet carries two token blocks: the base {@code .root} palette (dark
 * graphite) and {@code .root.light}. A skin is therefore just one style class
 * on the scene root; no per-screen wiring and no second sheet.
 */
public final class Theme {

    /** Classpath location of the sheet, beside the FXML views. */
    public static final String STYLESHEET =
        "/com/xdev/gest_comm_clientfx/views/industrial.css";

    /** Style class that selects the light token block. */
    private static final String LIGHT_CLASS = "light";

    /** Palette choice. {@link #DARK} is the base block of the sheet. */
    public enum Skin {
        DARK,
        LIGHT
    }

    private Theme() { }

    /** Attach the theme to a scene, idempotently, in the default dark skin. */
    public static void install(Scene scene) {
        install(scene, Skin.DARK);
    }

    /** Attach the theme and select a palette, idempotently. */
    public static void install(Scene scene, Skin skin) {
        if (scene == null) {
            return;
        }
        URL sheet = Theme.class.getResource(STYLESHEET);
        if (sheet == null) {
            throw new IllegalStateException("Missing theme stylesheet " + STYLESHEET);
        }
        String url = sheet.toExternalForm();
        if (!scene.getStylesheets().contains(url)) {
            scene.getStylesheets().add(url);
        }
        applySkin(scene, skin);
    }

    /** Switch palettes on a scene that already carries the sheet. */
    public static void applySkin(Scene scene, Skin skin) {
        if (scene == null) {
            return;
        }
        if (scene.getRoot() == null) {
            throw new IllegalStateException(
                "Cannot select a skin before the scene has a root");
        }
        ObservableList<String> classes = scene.getRoot().getStyleClass();
        boolean light = skin == Skin.LIGHT;
        if (light && !classes.contains(LIGHT_CLASS)) {
            classes.add(LIGHT_CLASS);
        } else if (!light && classes.contains(LIGHT_CLASS)) {
            classes.remove(LIGHT_CLASS);
        }
    }
}
