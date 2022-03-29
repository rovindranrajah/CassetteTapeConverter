package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CassetteTapeConverter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Controller controller = (Controller)fxmlLoader.getController();
        controller.setStage(stage);
        stage.setTitle("Cassette Tape Converter");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    @Override
    public void stop() throws IOException {
        Runtime.getRuntime().exec("java -jar FileDeletor.jar");
    }



    public static void main(String[] args) {
        launch();
    }
}
