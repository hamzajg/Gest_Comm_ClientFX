package com.xdev.gest_comm_clientfx.ui;

import com.xdev.gest_comm_clientfx.i18n.Messages;
import javafx.fxml.FXMLLoader;

/**
 * Loads a view with the current language bundle, so {@code text="%key"}
 * resolves to the language chosen on this workstation. Every FXML loader in
 * the app goes through here; the bundle is re-read per load, which is what
 * makes the language switch work by rebuilding the scene (D6).
 */
public final class Views {

    private static final String ROOT = "/com/xdev/gest_comm_clientfx/views/";

    private Views() { }

    public static FXMLLoader loader(Class<?> anchor, String fxml) {
        FXMLLoader loader = new FXMLLoader(anchor.getResource(ROOT + fxml));
        loader.setResources(Messages.resourceBundle());
        return loader;
    }
}
