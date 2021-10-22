package sample;


import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


public class Controller implements Initializable {
    @FXML
    private ChoiceBox<String> audioLine;
    @FXML
    private Label status;
    @FXML
    public Label hourLabel, minuteLabel, secondLabel, milliLabel;
    @FXML
    private MenuItem saveWindow;
    @FXML private ProgressBar musicProgress;

    Integer num = 1;

    private StopwatchTimer timer;
    String dir = "rawFile";
    File directory = new File(dir);
    String path = "recording.wav";
    Microphone microphone = new Microphone(dir + "/" + path);
    MediaPlayer player;
    boolean play = false;
    Timer progressTimer;
    TimerTask progressTask;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        timer.startTimer();
        microphone.startRecording();
        status.setText("Status: Recording....");
        //timer.scheduleAtFixedRate(task, 0, 1000);
    }

    public void stop(ActionEvent e) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        microphone.stopRecording();
        timer.stopTimer();
        status.setText("Status: Stopped Recording....");
        player = AudioUtils.getRecording(new File("converted/recording.mp3"));
        //timer.cancel();
    }

    public void playPause(ActionEvent e) {
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
                    Mp3File file = new Mp3File(new File("converted/recording.mp3"));
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
        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("ID3Tags.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage saveWin = new Stage();
        saveWin.setTitle("ID3 Tags Editor");
        saveWin.setScene(scene);
        saveWin.setResizable(false);
        saveWin.show();
    }
}