package ru.alex9127;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("ui.fxml")));
        stage.setScene(new Scene(root));
        stage.setTitle("CGTask2");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}