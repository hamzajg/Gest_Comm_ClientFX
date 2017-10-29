package com.xdev.gest_comm_clientfx.app;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class HomeScreen {

    private Parent rootView;
    public HomeScreen() throws IOException {
        rootView = FXMLLoader.load(getClass().getResource("/com/xdev/gest_comm_clientfx/views/HomeScreen.fxml"));
    }
    public Parent getRootView() {
        return rootView;
    }
}
