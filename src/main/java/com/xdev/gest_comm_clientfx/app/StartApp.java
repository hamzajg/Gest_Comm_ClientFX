package com.xdev.gest_comm_clientfx.app;

import com.xdev.gest_comm_clientfx.ui.Theme;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        primaryStage.setTitle("Gestion Commerciale");
        Scene scene = new Scene(new HomeScreen().getRootView(), 1200, 700);
        Theme.install(scene);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(640);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
