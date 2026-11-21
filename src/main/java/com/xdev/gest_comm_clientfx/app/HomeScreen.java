package com.xdev.gest_comm_clientfx.app;

import com.xdev.gest_comm_clientfx.controllers.HomeScreenCtrl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class HomeScreen {

    private final Parent rootView;
    private final HomeScreenCtrl controller;

    public HomeScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml"));
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
}
