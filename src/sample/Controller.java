package sample;


import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.InputFormatException;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Controller implements Initializable {
    @FXML
    private ChoiceBox<String> audioLine;
    @FXML
    private MenuItem reprocessItem;

    @FXML
    public Label hourLabel, minuteLabel, secondLabel, milliLabel,volume2;
    @FXML private ProgressBar musicProgress;
    @FXML private Button stopButton, startButton, playPause;
    @FXML private Slider volumeSlider;
    public static boolean recorded = false;
    private Stage stage;
    private File splitDirectory = new File("System/splitted");
    private File[] files;
    private LinkedList<File> songs = new LinkedList<File>();
    private LinkedList<MediaPlayer> players = new LinkedList<MediaPlayer>();
    private LinkedList<CheckBox> checkBoxes = new LinkedList<CheckBox>();
    private LinkedList<Button> buttons = new LinkedList<Button>();
    private LinkedList<Slider> sliders = new LinkedList<Slider>();
    Timer observer;
    @FXML
    BorderPane root = new BorderPane();
    @FXML
    public CheckBox audioPassthrough;
    @FXML
    public Label statusLabel;
    @FXML
    public ProgressBar statusProgressBar;



    private StopwatchTimer timer;
    String dir = "System/rawFile";
    File directory = new File(dir);
    String path = "recording.wav";
    public static String saveDirectory;
    Microphone microphone = new Microphone(path, dir);
    MediaPlayer player;
    boolean play = false;
    Timer progressTimer;
    TimerTask progressTask;
    private double volume;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
       // stopButton.prefWidthProperty().bind(anchorPane.widthProperty());

        //anchorPane.prefWidthProperty().bind(borderPane.widthProperty());
        stopButton.setDisable(true);
        playPause.setDisable(true);
        reprocessItem.setDisable(true);
        volumeSlider.setDisable(true);
        new File("System").mkdir();
        directory.mkdir();
        audioLine.getItems().addAll(microphone.getTargetLines().keySet());
        audioLine.setValue("Default");
        audioLine.setOnAction(this::changeLine);
        hourLabel.setText("00");
        timer = new StopwatchTimer(hourLabel, minuteLabel, secondLabel, milliLabel);

    }
    public void setStage(Stage stage){
        this.stage = stage;
        this.stage.setOnCloseRequest(evt -> {
            // prevent window from closing
            if(recorded){
                Alert.AlertType type = Alert.AlertType.CONFIRMATION;
                Alert alert = new Alert(type,"");
                alert.setTitle("Are You Sure?");
                alert.setHeaderText("Recordings Not Saved");
                alert.setContentText("You have files that have not be saved. Exiting will cause a permanent lost in files. Are you sure?");
                alert.setGraphic(null);
                //alert.show();
                Optional<ButtonType> result = alert.showAndWait();
                if(!(result.get() == ButtonType.OK)){
                    evt.consume();
                }
            }
            // execute own shutdown procedure
        });
    }
    public void changeLine(ActionEvent event) {
        microphone.setTargetLine(audioLine.getValue());
    }

    public void start() throws LineUnavailableException, InterruptedException {
        if(player!=null){
            Alert.AlertType type = Alert.AlertType.CONFIRMATION;
            Alert alert = new Alert(type,"");
            alert.setTitle("Are You Sure?");
            alert.setHeaderText("Recording Already Exists");
            alert.setContentText("This will overwrite the previously recorded file");
            alert.setGraphic(null);
            //alert.show();
            Optional<ButtonType> result = alert.showAndWait();
            if(result.get() == ButtonType.OK){
                try {
                    player.stop();
                    player.dispose();
                    closePlayers();
                    players.clear();
                    buttons.clear();
                    sliders.clear();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                File source = new File("System/splitted");
                File[] files;
                if(source.exists()){
                    files = source.listFiles();
                    for(File file : files){
                        file.delete();
                    }
                }

                source = new File("System/rawFile");
                if(source.exists()){
                    files = source.listFiles();
                    for(File file : files){
                        file.delete();
                    }
                }

                source = new File("System/converted");
                if(source.exists()){
                    files = source.listFiles();
                    for(File file : files){
                        file.delete();
                    }
                }

                source = new File("System/mp3");
                if(source.exists()){
                    files = source.listFiles();
                    for(File file : files){
                        file.delete();
                    }
                }
                recorded=false;
                timer.startTimer();
                startButton.setDisable(true);
                stopButton.setDisable(false);   
                playPause.setDisable(true);
                volumeSlider.setDisable(true);
                statusLabel.setText("Recording...");
                statusProgressBar.setProgress(-1);
                microphone.startRecording(this);
            }

        }
        else {
            recorded = false;
            timer.startTimer();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            statusLabel.setText("Recording...");
            statusProgressBar.setProgress(-1);
            microphone.startRecording(this);
        }
    }

    public void stop() throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        timer.stopTimer();
        microphone.stopRecording();
        recorded = true;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                statusLabel.setText("Splitting Audio...");
            }
        });

    }
/*    public static void updateStatus(){
        Timer timer = new Timer
    }
    public static void updateProgress(){

    }*/
    public void loadRecording() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        startButton.setDisable(false);
        reprocessItem.setDisable(false);
        stopButton.setDisable(true);
        playPause.setDisable(false);
        volumeSlider.setDisable(false);
        player = AudioUtils.getRecording(new File("System/rawFile/recording.wav"));
        setVolume();
        volumeSlider.setDisable(false);
        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setVolume();
            }
        });
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        statusProgressBar.setProgress(1);
        statusLabel.setText("DONE AUTO SPLIT!");
        setRoot(root);
    }

    public void setVolume(){
        volume = volumeSlider.getValue() / 100;
        player.setVolume(volume);
    }

    public void playPause(ActionEvent e) throws IOException {
        if (player.getCurrentTime().toSeconds() == player.getTotalDuration().toSeconds()){
            player.seek(new Duration(0));
            resetProgress();
            stopTimer();
        }
        if (play) {

            //clip.stop();
            player.pause();
            stopTimer();
            play = false;


        } else {
            //clip.start();
            player.play();
            beginProgress();
            play = true;
        }
    }

    public void beginProgress(){
        progressTimer = new Timer();
        progressTask = new TimerTask() {
            @Override
            public void run() {
                double current = player.getCurrentTime().toSeconds();
                try {
                    Mp3File file = new Mp3File(new File("System/converted/recording.mp3"));
                    double total = file.getLengthInSeconds();
                    musicProgress.setProgress((current/total));
                    if((current/total)>=1){
                        stopTimer();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedTagException e) {
                    e.printStackTrace();
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
            }
        };
        progressTimer.scheduleAtFixedRate(progressTask, 0, 10);
    }
    public void resetProgress() {
        musicProgress.setProgress(0);
    }
    public void stopTimer() {
        progressTimer.cancel();
        progressTimer.purge();
    }
    public void reprocessTracks(){
        statusLabel.setText("Reprocessing Audio...");
        statusProgressBar.setProgress(-1);
        try {
            closePlayers();
            players.clear();
            buttons.clear();
            sliders.clear();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File source = new File("System/splitted");
        File[] files = source.listFiles();
        for(File file : files){
            file.delete();
        }
        Thread processor = new Thread(new Processor());
        processor.start();
        observer = new Timer();
        TimerTask reprocessTask = new TimerTask() {
            @Override
            public void run() {
                if(!processor.isAlive()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            statusProgressBar.setProgress(1);
                            statusLabel.setText("DONE REPROCESS!");
                            setRoot(root);
                        }
                    });

                    observer.cancel();
                    observer.purge();
                }
            }
        };
        observer = new Timer();
        observer.scheduleAtFixedRate(reprocessTask, 250, 250);
    }
    public void openSaveWindow() throws IOException, InterruptedException {
        TextInputDialog albumDialog = new TextInputDialog();
        int counter = 0;
        albumDialog.setTitle("Album Name");
        albumDialog.setHeaderText("Enter the album name: ");
        albumDialog.setContentText("Name: ");
        albumDialog.setGraphic(null);
        Optional<String> result = albumDialog.showAndWait();
        saveDirectory = result.get();
        //System.out.println(result.get());
        Thread t1 = new Thread(() -> convertTracks());
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(t1.isAlive()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            statusProgressBar.setProgress(-1);
                            statusLabel.setText("Converting to mp3...");
                        }
                    });

                    return;
                }
                else{
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                statusProgressBar.setProgress(1);
                                statusLabel.setText("DONE CONVERSION!");
                                saveWindow();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    observer.cancel();
                    observer.purge();
                }
            }
        };
        observer = new Timer();
        observer.scheduleAtFixedRate(timerTask, 250, 250);
        t1.start();


    }
    public void helpWindow() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("Help.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage saveWin = new Stage();
        saveWin.setTitle("How to Use");
        saveWin.setScene(scene);
        saveWin.setResizable(false);
        saveWin.show();
    }

    public void saveWindow() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("ID3Tags.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage saveWin = new Stage();
        saveWin.setTitle("ID3 Tags Editor");
        saveWin.setScene(scene);
        saveWin.setResizable(false);
        saveWin.show();
    }
    public void openSettingsWindow() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage saveWin = new Stage();
        saveWin.setTitle("Settings");
        saveWin.setScene(scene);
        saveWin.setResizable(false);
        saveWin.show();
    }
    public void convertTracks(){
        File source = new File("System/splitted");
        File destination = new File("System/mp3");
        File[] files = source.listFiles();
        for(File file : files){
            try {
                String name = file.getName().replaceFirst("[.][^.]+$", ".mp3") ;
                AudioUtils.wav2mp3(file.getPath(), destination+"/"+name, Microphone.BIT_RATE, Microphone.CHANNEL, Microphone.SAMPLE_RATE);

            } catch (InputFormatException e) {
                e.printStackTrace();
            } catch (EncoderException e) {
                e.printStackTrace();
            }

        }
    }
    //Merge
    public void handleMerge(LinkedList<CheckBox> checkBoxes, LinkedList<File> songs) throws InterruptedException {
        LinkedList<File> selectedSongs = new LinkedList<File>();
        boolean nameIsSet = false;
        Alert.AlertType type = Alert.AlertType.ERROR;
        Alert alert = new Alert(type,"");
        alert.setTitle("ERROR IN MERGING");
        String name="";
        //progressBar.setProgress(0.3);
        for(int i=0; i<checkBoxes.size(); i++){
            if(checkBoxes.get(i).isSelected()){
                selectedSongs.add(songs.get(i));
                if(!nameIsSet){
                    name = songs.get(i).getName().replaceFirst("[.][^.]+$", "") + "-merged.wav";
                    nameIsSet = true;
                }
            }
        }

        // ((Stage)root.getScene().getWindow()).close();


        String finalName = name;

        Thread t1 = new Thread(){
            @Override
            public void run(){
                AudioUtils.mergeTracks(finalName, selectedSongs);
            }
        };
        if(selectedSongs.size() == 0){

            alert.setHeaderText("Tracks Merging Error: No Tracks Selected");
            alert.setContentText("You have selected no tracks to merge. Please choose at least 2 tracks to split.");
            alert.show();
            return;
        }else if(selectedSongs.size() == 1){
            alert.setHeaderText("Tracks Merging Error: Insufficient Tracks Selected");
            alert.setContentText("You have selected only 1 track to merge. Please choose at least 2 tracks to split.");
            alert.show();
            return;
        }
        closePlayers();
        players.clear();
        buttons.clear();
        sliders.clear();

        t1.start();
        while(true){
            if(t1.isAlive()){

                continue;
            }
            else{

                setRoot(root);
                break;
            }

        }
    }

    public void handleSplit(LinkedList<CheckBox> checkBoxes, LinkedList<File> songs) throws IOException, UnsupportedAudioFileException, InterruptedException {
        LinkedList<File> song = new LinkedList<File>();
        double splitDuration=0;
        Alert.AlertType type = Alert.AlertType.ERROR;
        Alert alert = new Alert(type,"");
        alert.setTitle("ERROR IN SPLITTING");

        for(int i=0; i<checkBoxes.size(); i++){
            if(checkBoxes.get(i).isSelected()){
                song.add(songs.get(i));
                splitDuration = sliders.get(i).getValue();
                if(song.size() > 1){

                    alert.setHeaderText("Tracks Splitting Error: Count Exceeded");
                    alert.setContentText("You have selected more than 1 track to split. Please choose only 1 track to split.");
                    alert.show();
                    return;
                }

                if(sliders.get(i).getValue() == sliders.get(i).getMin() || sliders.get(i).getValue() == sliders.get(i).getMax()){

                    alert.setHeaderText("Tracks Splitting Error: Invalid Duration");
                    alert.setContentText("You set the seeker at the minimum or maximum value. Please set at the right position to split");
                    alert.show();
                    return;
                }
            }
        }
        if(song.size() == 0){

            alert.setHeaderText("Tracks Splitting Error: No Tracks Selected");
            alert.setContentText("You have selected no tracks to split. Please choose 1 track to split.");
            alert.show();
            return;
        }else{
            double finalSplitDuration = splitDuration;
            Thread t1 = new Thread(){
                @Override
                public void run(){
                    try {
                        AudioUtils.splitTrack(song.get(0), finalSplitDuration /1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (UnsupportedAudioFileException e) {
                        e.printStackTrace();
                    }
                }
            };
            closePlayers();
            players.clear();
            buttons.clear();
            sliders.clear();
            t1.start();
            while(true){
                if(t1.isAlive()){
                    //progressBar.setProgress(progressBar.getProgress()+0.1);
                    continue;
                }
                else{
                    // progressBar.setProgress(1.0);
                    setRoot(root);
                    break;
                }
            }
        }

    }
    public void deleteTracks(){
        LinkedList<File> selectedSongs = new LinkedList<File>();
        Alert.AlertType type = Alert.AlertType.ERROR;
        Alert alert = new Alert(type,"");
        alert.setTitle("ERROR IN DELETING");
        String name="";
        //progressBar.setProgress(0.3);
        for(int i=0; i<checkBoxes.size(); i++){
            if(checkBoxes.get(i).isSelected()){
                selectedSongs.add(songs.get(i));
            }
        }
        if(selectedSongs.size() == 0) {

            alert.setHeaderText("Deleting Tracks Error: No Tracks Selected");
            alert.setContentText("You have selected no tracks to delete. Please choose at least 1 track to delete.");
            alert.show();
            return;
        }
        try {
            closePlayers();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        players.clear();
        buttons.clear();
        sliders.clear();
        for(File f:selectedSongs){
            f.delete();
        }
        setRoot(root);
    }
    public void closePlayers() throws InterruptedException {
        for(MediaPlayer p: players){
            p.stop();
            p.dispose();
        }
        System.gc();
        TimeUnit.MILLISECONDS.sleep(250);
    }
    public void setRoot(BorderPane root){
        if (!songs.isEmpty()){
            songs.clear();

        }

        try {

            players = new LinkedList<MediaPlayer>();
            checkBoxes = new LinkedList<CheckBox>();
            buttons = new LinkedList<Button>();
            GridPane grid = new GridPane();


            // Create JavaFX Controls (nodes) to add to the GridPane
            Label boxLabel = new Label("Select");
            Label trackName = new Label("Track Name");
            Label sliderName = new Label("Seek");
            Label playLabel = new Label("Play/Pause");


            Button btnMerge = new Button("Merge");
            btnMerge.setOnAction(e-> {
                try {
                    handleMerge(checkBoxes, songs);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            });
            Button btnSplit = new Button("Split");
            btnSplit.setOnAction(e-> {
                try {
                    handleSplit(checkBoxes, songs);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (UnsupportedAudioFileException | InterruptedException unsupportedAudioFileException) {
                    unsupportedAudioFileException.printStackTrace();
                }
            });
            Button saveTracks = new Button("Save");
            saveTracks.setOnAction(e-> {
                try {
                    openSaveWindow();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            });
            Button delete = new Button("Delete");
            delete.setOnAction(e-> {
                deleteTracks();
            });
            VBox vBox = new VBox();

            HBox hBox = new HBox();
            hBox.setSpacing(40);
            hBox.getChildren().addAll(btnMerge, btnSplit, saveTracks, delete);
            hBox.setAlignment(Pos.CENTER);

            vBox.getChildren().add(hBox);


            grid.add(boxLabel, 0, 0, 1, 1);
            grid.add(trackName, 1, 0, 1, 1);
            grid.add(sliderName, 2, 0, 1, 1);
            grid.add(playLabel, 3, 0, 1, 1);


            // Set Column and Row Gap
            grid.setHgap(10);
            grid.setVgap(5);

            grid.setPadding(new Insets(10, 10, 10, 10));

            // Column Constraints
            ColumnConstraints column1 = new ColumnConstraints();
            ColumnConstraints column2 = new ColumnConstraints();
            ColumnConstraints column3= new ColumnConstraints();
            ColumnConstraints column4 = new ColumnConstraints();
            column1.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(column1);
            grid.getColumnConstraints().add(column2);
            grid.getColumnConstraints().add(column3);
            grid.getColumnConstraints().add(column4);

            column2.setPrefWidth(300);
            column3.setPrefWidth(200);

            files = splitDirectory.listFiles();
            if(files!=null){
                for(File file : files){
                    songs.add(file);
                    //System.out.println(file.getName());
                    players.add(AudioUtils.getRecording(file));

                    sliders.add(new Slider(0,AudioUtils.getDuration(file),0));
                }

            }

            for(int i=0; i < songs.size(); i++){
                checkBoxes.add(new CheckBox());
                grid.add(checkBoxes.get(i), 0, 1+i, 1, 1);
                grid.add(new Label(songs.get(i).getName()), 1, 1+i, 1, 1);
                final Boolean[] played = {false};
                buttons.add(new Button("Play"));
                int finalI = i;
                buttons.get(finalI).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        if(played[0]){
                            players.get(finalI).pause();
                            //sliders.get(finalI).setValue(0);
                            buttons.get(finalI).setText("Play");
                            //sliders.get(finalI).setDisable(true);
                            played[0] = false;
                        }else{
                            for(int i=0; i<buttons.size(); i++){
                                players.get(i).pause();
                                //sliders.get(i).setValue(0);
                                buttons.get(i).setText("Play");
                                //sliders.get(i).setDisable(true);
                            }

                            players.get(finalI).play();
                            //sliders.get(finalI).setDisable(false);
                            buttons.get(finalI).setText("Pause");
                            played[0] = true;
                        }

                    }
                });
                //int finalI1 = i;
                players.get(i).currentTimeProperty().addListener(new InvalidationListener() {
                    public void invalidated(Observable ov) {
                        Platform.runLater(new Runnable() {
                            public void run() {
                                if(!sliders.get(finalI).isValueChanging()){
                                    sliders.get(finalI).setValue(players.get(finalI).getCurrentTime().toMillis());
                                }
                                sliders.get(finalI).setValue(players.get(finalI).getCurrentTime().toMillis());
                            }
                        });
                        //sliders.getLast().setValue(players.getLast().getCurrentTime().toMillis());
                    }
                });
                sliders.get(i).valueProperty().addListener(new InvalidationListener() {
                    /*@Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        //sliderName.setText(newValue.toString());
                        players.get(finalI).seek(new Duration(newValue.doubleValue()));
                        //System.out.println(newValue);
                    }*/
                    @Override
                    public void invalidated(Observable ov) {
                        if (sliders.get(finalI).isValueChanging()) {
                            // multiply duration by percentage calculated by slider position
                            players.get(finalI).seek(new Duration(sliders.get(finalI).getValue()));
                        }
                    }
                });
                //sliders.get(i).setDisable(true);
                grid.add(sliders.get(i), 2, 1+i, 1, 1);
                grid.add(buttons.get(i), 3, 1+i, 1, 1);
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