package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class Main implements Initializable {
    private File directory = new File("System/splitted");
    private File[] files;
    private LinkedList<File> songs = new LinkedList<File>();
    private LinkedList<MediaPlayer> players = new LinkedList<MediaPlayer>();
    private LinkedList<CheckBox> checkBoxes = new LinkedList<CheckBox>();
    @FXML
    BorderPane root = new BorderPane();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       setRoot(root);

    }

    public void handleMerge(LinkedList<CheckBox> checkBoxes, LinkedList<File> songs){
        LinkedList<File> selectedSongs = new LinkedList<File>();
        boolean nameIsSet = false;
        String name="";

        for(int i=0; i<checkBoxes.size(); i++){
            if(checkBoxes.get(i).isSelected()){
                selectedSongs.add(songs.get(i));
                if(!nameIsSet){
                    name = songs.get(i).getName().replaceFirst("[.][^.]+$", "") + "-merged.mp3";
                    nameIsSet = true;
                }
            }
        }

        AudioUtils.mergeTracks(name, selectedSongs);
        setRoot(root);

    }

    public void setRoot(BorderPane root){
        if (!songs.isEmpty()){
            songs.clear();

        }

        try {

            players = new LinkedList<MediaPlayer>();
            checkBoxes = new LinkedList<CheckBox>();
            GridPane grid = new GridPane();


            // Create JavaFX Controls (nodes) to add to the GridPane
            Label boxLabel = new Label("Select");
            Label trackName = new Label("Track Name");
            Label playLabel = new Label("Play/Pause");


            Button btnMerge = new Button("Merge");
            btnMerge.setOnAction(e->handleMerge(checkBoxes, songs));
            Button btnCancel = new Button("Close");
            Button saveTracks = new Button("Save");
            VBox vBox = new VBox();

            HBox hBox = new HBox();
            hBox.setSpacing(40);
            hBox.getChildren().addAll(btnMerge, btnCancel, saveTracks);
            hBox.setAlignment(Pos.CENTER);

            vBox.getChildren().add(hBox);


            grid.add(boxLabel, 0, 0, 1, 1);
            grid.add(trackName, 1, 0, 1, 1);
            grid.add(playLabel, 2, 0, 1, 1);


            // Set Column and Row Gap
            grid.setHgap(10);
            grid.setVgap(5);

            grid.setPadding(new Insets(10, 10, 10, 10));

            // Column Constraints
            ColumnConstraints column1 = new ColumnConstraints();
            ColumnConstraints column2 = new ColumnConstraints();
            column1.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(column1);
            grid.getColumnConstraints().add(column2);

            column2.setPrefWidth(200);

            files = directory.listFiles();
            if(files!=null){
                for(File file : files){
                    songs.add(file);
                    players.add(AudioUtils.getRecording(file));
                }

            }

            for(int i=0; i < songs.size(); i++){
                checkBoxes.add(new CheckBox());
                grid.add(checkBoxes.get(i), 0, 1+i, 1, 1);
                grid.add(new Label(songs.get(i).getName()), 1, 1+i, 1, 1);
                final Boolean[] played = {false};
                Button button = new Button("Play");
                int finalI = i;
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if(played[0]){
                            players.get(finalI).stop();
                            button.setText("Play");
                            played[0] = false;
                        }else{
                            players.get(finalI).play();
                            button.setText("Stop");
                            played[0] = true;
                        }

                    }
                });

                grid.add(button, 2, 1+i, 1, 1);
            }

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(grid);
            root.setCenter(scrollPane);
            root.setBottom(vBox);

            root.setAlignment(vBox,Pos.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}