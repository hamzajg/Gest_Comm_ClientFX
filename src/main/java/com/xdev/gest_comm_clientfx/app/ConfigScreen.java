package com.xdev.gest_comm_clientfx.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class ConfigScreen {

    private Parent rootView;
    public ConfigScreen() throws IOException {
        rootView = FXMLLoader.load(getClass().getResource("/com/xdev/gest_comm_clientfx/views/ConfigScreen.fxml"));
    }
    public Parent getRootView() {
        return rootView;
    }
}
