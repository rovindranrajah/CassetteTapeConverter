package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CassetteTapeConverter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Cassette Tape Converter");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    @Override
    public void stop() throws IOException, InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        FileUtils.deleteDirectory(new File("rawFile"));
        //FileUtils.deleteDirectory(new File("splitted"));
        //FileUtils.deleteDirectory(new File("converted"));
        //System.out.print(new File("converted/recording.mp3").delete());
    }

    public static void main(String[] args) {
        launch();
    }
}
