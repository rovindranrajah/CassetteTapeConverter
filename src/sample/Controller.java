package sample;


import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


public class Controller implements Initializable {
    @FXML
    private ChoiceBox<String> audioLine;
    @FXML
    public Label hourLabel, minuteLabel, secondLabel, milliLabel;
    @FXML private ProgressBar musicProgress;
    @FXML private Button stopButton, startButton, playPause;
    @FXML private MenuItem saveWindow;


    Integer num = 1;

    private StopwatchTimer timer;
    String dir = "System/rawFile";
    File directory = new File(dir);
    String path = "recording.wav";
    public static String saveDirectory;
    Microphone microphone = new Microphone(dir + "/" + path);
    MediaPlayer player;
    boolean play = false;
    Timer progressTimer;
    TimerTask progressTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stopButton.setDisable(true);
        playPause.setDisable(true);
        saveWindow.setDisable(true);
        new File("System").mkdir();
        directory.mkdir();
        audioLine.getItems().addAll(microphone.getTargetLines().keySet());
        audioLine.setValue("Default");
        audioLine.setOnAction(this::changeLine);
        hourLabel.setText("00");
        timer = new StopwatchTimer(hourLabel, minuteLabel, secondLabel, milliLabel);
        //timer = new StopwatchTimer(status);
    }

    public void changeLine(ActionEvent event) {
        microphone.setTargetLine(audioLine.getValue());
    }

    public void start(ActionEvent e) throws LineUnavailableException {
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
                timer.startTimer();
                startButton.setDisable(true);
                stopButton.setDisable(false);
                playPause.setDisable(true);
                microphone.startRecording();
            }

        }
        else {
            timer.startTimer();
            startButton.setDisable(true);
            stopButton.setDisable(false);
            microphone.startRecording();
        }
    }

    public void stop(ActionEvent e) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        microphone.stopRecording();
        startButton.setDisable(false);
        stopButton.setDisable(true);
        playPause.setDisable(false);
        saveWindow.setDisable(false);
        timer.stopTimer();
        player = AudioUtils.getRecording(new File("System/converted/recording.mp3"));
        //timer.cancel();
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
    public void openSaveWindow() throws IOException {
        TextInputDialog albumDialog = new TextInputDialog();
        albumDialog.setTitle("Album Name");
        albumDialog.setHeaderText("Enter the album name: ");
        albumDialog.setContentText("Name: ");
        albumDialog.setGraphic(null);
        Optional<String> result = albumDialog.showAndWait();
        saveDirectory = result.get();
        //System.out.println(result.get());
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
}