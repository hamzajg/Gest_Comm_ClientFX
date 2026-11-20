package com.xdev.gest_comm_clientfx.ui;

import javafx.scene.Scene;

import java.net.URL;

/**
 * The application's industrial stylesheet.
 *
 * The whole UI lives in one {@link Scene}, so installing the sheet once there
 * themes every tab-hosted screen (lists, forms, settings, assistant). Colours
 * live only in {@code views/industrial.css} — see specs/industrial-look-feel.
 */
public final class Theme {

    /** Classpath location of the sheet, beside the FXML views. */
    public static final String STYLESHEET =
        "/com/xdev/gest_comm_clientfx/views/industrial.css";

    private Theme() { }

    /** Attach the theme to a scene, idempotently. */
    public static void install(Scene scene) {
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
    }
}
