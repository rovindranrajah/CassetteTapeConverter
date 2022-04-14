package sample;

import com.mpatric.mp3agic.*;


import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import java.awt.font.LineMetrics;
import java.io.*;
import java.net.URL;
import java.util.*;

import java.util.concurrent.TimeUnit;

import static sample.Controller.saveDirectory;

public class ID3Tags implements Initializable {
    private static final String COMMA_DELIMITER = ",";
    private File directory = new File("System/mp3");
    private File[] files;
    private LinkedList<File> songs = new LinkedList<File>();
    private LinkedList<MediaPlayer> players = new LinkedList<MediaPlayer>();
    private LinkedList<TextField> titleFields = new LinkedList<TextField>();
    private LinkedList<TextField> artistFields = new LinkedList<TextField>();
    private LinkedList<TextField> albumFields = new LinkedList<TextField>();
    private LinkedList<TextField> genreFields = new LinkedList<TextField>();
    private LinkedList<TextField> yearFields = new LinkedList<TextField>();
    private LinkedList<TextField> trackFields = new LinkedList<TextField>();

    private LinkedList<Button> playPauseButtons = new LinkedList<Button>();
    @FXML
    BorderPane root = new BorderPane();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       setRoot(root);
        new File(saveDirectory).mkdir();
    }

    public void saveTags() throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {
        for(MediaPlayer m: players){
            m.stop();
            m.dispose();
        }

        System.gc();
        try {
            TimeUnit.MILLISECONDS.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(File f: songs){
            Mp3File file = new Mp3File(f);
            ID3v2 tag = new ID3v24Tag();
            file.setId3v2Tag(tag);
            String name = titleFields.pollFirst().getText();
            tag.setTitle(name);
            tag.setAlbum(albumFields.pollFirst().getText());
            tag.setArtist(artistFields.pollFirst().getText());
            tag.setYear(yearFields.pollFirst().getText());
            tag.setTrack(trackFields.pollFirst().getText());
            tag.setGenreDescription(genreFields.pollFirst().getText());
            file.save(saveDirectory+"/"+ name+".mp3");
        }
        Controller.recorded = false;
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();

    }
    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                String current = rowScanner.next();
                if(current.length() !=0){
                    if(current.charAt(0) == '\"'){
                        //System.out.println("Need check");
                        while(current.charAt(current.length()-1) != '\"'){
                            current += "," + rowScanner.next();
                            //System.out.println("Concat");
                        }
                        StringBuilder sb = new StringBuilder(current);
                        sb.deleteCharAt(0);
                        sb.deleteCharAt(sb.length()-1);
                        current = sb.toString();
                    }
                }

                values.add(current);
            }
        }
        return values;
    }

    public void setRoot(BorderPane root){
        if (!songs.isEmpty()){
            songs.clear();

        }

        try {

            players = new LinkedList<MediaPlayer>();
            GridPane grid = new GridPane();
            Button importCSV = new Button("Import CSV");
            importCSV.setOnAction(e-> {
                Stage stage = new Stage();
                stage.setTitle("Select CSV file");
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV File", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                fileChooser.setTitle("Select CSV file");
                File file = fileChooser.showOpenDialog(stage);

                if(file != null){
                    List<List<String>> records = new ArrayList<>();
                    try {
                        Scanner scanner = new Scanner(file);
                        while (scanner.hasNextLine()) {
                            records.add(getRecordFromLine(scanner.nextLine()));
                        }
                    } catch (FileNotFoundException fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                    }
                    for(int i=0; i<records.size(); i++){
                        if(i < titleFields.size()){
                            titleFields.get(i).setText(records.get(i).get(0));
                            artistFields.get(i).setText(records.get(i).get(1));
                            albumFields.get(i).setText(records.get(i).get(2));
                            genreFields.get(i).setText(records.get(i).get(3));
                            yearFields.get(i).setText(records.get(i).get(4));
                            trackFields.get(i).setText(records.get(i).get(5));
                        }
                    }
                }

            });
            Button exportCSV = new Button("Export CSV");
            exportCSV.setOnAction(e-> {
                Stage stage = new Stage();
                stage.setTitle("Save CSV file");
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV File", "*.csv");
                fileChooser.getExtensionFilters().add(extFilter);
                fileChooser.setTitle("Choose save location");
                fileChooser.setInitialFileName("ID3Tags.csv");
                File file = fileChooser.showSaveDialog(stage);

                try {
                    Writer writer = new FileWriter(file);

                    for (int i=0; i<trackFields.size(); ++i) {
                        writer.write("\""+titleFields.get(i).getText() + "\",\"");
                        writer.write(artistFields.get(i).getText() + "\",\"");
                        writer.write(albumFields.get(i).getText() + "\",\"");
                        writer.write(genreFields.get(i).getText() + "\",\"");
                        writer.write(yearFields.get(i).getText() + "\",\"");
                        writer.write(trackFields.get(i).getText() + "\"");
                        writer.write("\n");
                    }

                    writer.close();
                }
                catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }

            });

            // Create JavaFX Controls (nodes) to add to the GridPane
            Label noLabel = new Label("No.");
            Label titleLabel = new Label("Title");
            Label artistLabel = new Label("Artist");
            Label albumLabel = new Label("Album");
            Label genreLabel = new Label("Genre");
            Label yearLabel = new Label("Year");
            Label trackLabel = new Label("Track");
            Label playPauseLabel = new Label("Play/Pause");


            Button btnSave = new Button("Save");
            btnSave.setOnAction(e-> {
                try {
                    saveTags();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (NotSupportedException notSupportedException) {
                    notSupportedException.printStackTrace();
                } catch (UnsupportedTagException unsupportedTagException) {
                    unsupportedTagException.printStackTrace();
                } catch (InvalidDataException invalidDataException) {
                    invalidDataException.printStackTrace();
                }
            });

            VBox vBox = new VBox();

            HBox hBox = new HBox();
            hBox.setSpacing(40);
            hBox.getChildren().addAll(btnSave);
            hBox.setAlignment(Pos.CENTER);

            vBox.getChildren().add(hBox);


            grid.add(noLabel, 0, 0, 1, 1);
            grid.add(titleLabel, 1, 0, 1, 1);
            grid.add(artistLabel, 2, 0, 1, 1);
            grid.add(albumLabel, 3, 0, 1, 1);
            grid.add(genreLabel, 4, 0, 1, 1);
            grid.add(yearLabel, 5, 0, 1, 1);
            grid.add(trackLabel, 6, 0, 1, 1);
            grid.add(playPauseLabel, 7, 0, 1, 1);


            // Set Column and Row Gap
            grid.setHgap(10);
            grid.setVgap(5);

            grid.setPadding(new Insets(10, 10, 10, 10));

            // Column Constraints
            ColumnConstraints column1 = new ColumnConstraints();
            ColumnConstraints column2 = new ColumnConstraints();
            ColumnConstraints column3= new ColumnConstraints();
            ColumnConstraints column4 = new ColumnConstraints();
            ColumnConstraints column5 = new ColumnConstraints();
            ColumnConstraints column6 = new ColumnConstraints();
            ColumnConstraints column7 = new ColumnConstraints();
            ColumnConstraints column8 = new ColumnConstraints();
            column1.setHalignment(HPos.CENTER);
            column3.setHalignment(HPos.CENTER);
            column4.setHalignment(HPos.CENTER);
            column5.setHalignment(HPos.CENTER);
            column6.setHalignment(HPos.CENTER);
            column7.setHalignment(HPos.CENTER);
            column8.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(column1);
            grid.getColumnConstraints().add(column2);
            grid.getColumnConstraints().add(column3);
            grid.getColumnConstraints().add(column4);
            grid.getColumnConstraints().add(column5);
            grid.getColumnConstraints().add(column6);
            grid.getColumnConstraints().add(column7);
            grid.getColumnConstraints().add(column8);


            files = directory.listFiles();
            if(files!=null){
                for(File file : files){
                    songs.add(file);
                    players.add(AudioUtils.getRecording(file));
                    titleFields.add(new TextField());
                    artistFields.add(new TextField());
                    albumFields.add(new TextField());
                    genreFields.add(new TextField());
                    yearFields.add(new TextField());
                    trackFields.add(new TextField());
                }

            }

            for(int i=0; i < songs.size(); i++){
                grid.add(new Label(Integer.toString(i+1)), 0, 1+i, 1, 1);
                grid.add(titleFields.get(i), 1, 1+i, 1, 1);
                grid.add(artistFields.get(i), 2, 1+i, 1, 1);
                grid.add(albumFields.get(i), 3, 1+i, 1, 1);
                grid.add(genreFields.get(i), 4, 1+i, 1, 1);
                grid.add(yearFields.get(i), 5, 1+i, 1, 1);
                grid.add(trackFields.get(i), 6, 1+i, 1, 1);


                final Boolean[] played = {false};
                playPauseButtons.add(new Button("Play"));
                int finalI = i;
                playPauseButtons.get(finalI).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if(played[0]){
                            players.get(finalI).pause();
                            playPauseButtons.get(finalI).setText("Play");
                            played[0] = false;
                        }else{
                            for(int i=0; i<playPauseButtons.size(); i++){
                                players.get(i).pause();
                                playPauseButtons.get(i).setText("Play");
                                //sliders.get(i).setDisable(true);
                            }
                            players.get(finalI).play();
                            //sliders.get(finalI).setDisable(false);
                            playPauseButtons.get(finalI).setText("Pause");
                            played[0] = true;
                        }

                    }
                });

                grid.add(playPauseButtons.get(i), 7, 1+i, 1, 1);
            }

            int lastRow = grid.getRowCount()+1;
            Button artistAuto = new Button("Autofill");
            artistAuto.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for(TextField t: artistFields){
                        t.setText(artistFields.getFirst().getText());
                    }
                }
            });
            grid.add(artistAuto, 2, lastRow, 1, 1);
            Button albumAuto = new Button("Autofill");
            albumAuto.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for(TextField t: albumFields){
                        t.setText(albumFields.getFirst().getText());
                    }
                }
            });
            grid.add(albumAuto, 3, lastRow, 1, 1);
            Button genreAuto = new Button("Autofill");
            genreAuto.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for(TextField t: genreFields){
                        t.setText(genreFields.getFirst().getText());
                    }
                }
            });
            grid.add(genreAuto, 4, lastRow, 1, 1);
            Button yearAuto = new Button("Autofill");
            yearAuto.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    for(TextField t: yearFields){
                        t.setText(yearFields.getFirst().getText());
                    }
                }
            });
            grid.add(yearAuto, 5, lastRow, 1, 1);
            Button trackAuto = new Button("Autofill");
            trackAuto.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    int i=1;
                    for(TextField t: trackFields){
                        t.setText(Integer.toString(i++));
                    }
                }
            });
            grid.add(trackAuto, 6, lastRow, 1, 1);
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(grid);
            root.setCenter(scrollPane);
            HBox top = new HBox();
            top.getChildren().add(importCSV);
            top.getChildren().add(exportCSV);
            root.setTop(top);

            root.setBottom(vBox);
            //vBox.setPadding(new Insets(2,0,2,0));
            root.setAlignment(vBox,Pos.CENTER);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}