package com.xdev.gest_comm_clientfx.app;

import com.xdev.gest_comm_clientfx.controllers.HomeScreenCtrl;
import com.xdev.gest_comm_clientfx.ui.Views;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class HomeScreen {

    private final Parent rootView;
    private final HomeScreenCtrl controller;

    public HomeScreen() throws IOException {
        FXMLLoader loader = Views.loader(getClass(), "HomeScreen.fxml");
        rootView = loader.load();
        controller = loader.getController();
    }

    public Parent getRootView() {
        return rootView;
    }

    /** Wire the shell's sign-out back to the stage that owns the gate. */
    public void setOnSignedOut(Runnable onSignedOut) {
        controller.setOnSignedOut(onSignedOut);
    }

    /** A language change rebuilds this scene through the stage. */
    public void setOnLanguageChanged(Runnable onLanguageChanged) {
        controller.setOnLanguageChanged(onLanguageChanged);
    }
}
