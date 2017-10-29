package com.xdev.gest_comm_clientfx.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Gestion Commerciale");
        primaryStage.setScene(new Scene(new HomeScreen().getRootView(), 1200, 700));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
