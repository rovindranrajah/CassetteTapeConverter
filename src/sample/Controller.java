package sample;

import com.groupdocs.metadata.Metadata;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML private ChoiceBox <String> audioLine;
    @FXML private Label status;
    @FXML public  Label hourLabel, minuteLabel, secondLabel,  milliLabel;

    Integer num = 1;

    private StopwatchTimer timer;

    String path = "recording.wav";
    Microphone microphone = new Microphone(path);
    File recording;
    AudioInputStream audioStream;
    Clip clip;
    boolean play = false;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audioLine.getItems().addAll(microphone.getTargetLines().keySet());
        audioLine.setValue("Default");
        audioLine.setOnAction(this::changeLine);
        hourLabel.setText("00");
        timer = new StopwatchTimer(hourLabel, minuteLabel, secondLabel, milliLabel);
        //timer = new StopwatchTimer(status);
    }
    public void changeLine(ActionEvent event){
        microphone.setTargetLine(audioLine.getValue());
    }

    public void start(ActionEvent e) throws LineUnavailableException {
        timer.startTimer();
        microphone.startRecording();
        status.setText("Status: Recording....");
        //timer.scheduleAtFixedRate(task, 0, 1000);
    }
    public void stop(ActionEvent e) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        microphone.stopRecording();
        timer.stopTimer();
        status.setText("Status: Stopped Recording....");
        getRecording();
        //timer.cancel();
    }

    public void playPause(ActionEvent e){
        if(play){
            clip.stop();
            play = false;
        }
        else {
            clip.start();
            play = true;
        }
        if (clip.getMicrosecondLength() == clip.getMicrosecondPosition()){
            clip.setMicrosecondPosition(0);
        }
    }
     public void getRecording() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        recording = new File(path);
        audioStream = AudioSystem.getAudioInputStream(recording);
        clip = AudioSystem.getClip();
        clip.open(audioStream);
     }


}
