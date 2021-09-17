package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

import javax.sound.sampled.LineUnavailableException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;


public class Controller implements Initializable {
    @FXML private ChoiceBox <String> audioLine;
    @FXML private Label status;
    @FXML public  Label hourLabel, minuteLabel, secondLabel,  milliLabel;
    //Label status2;
    Integer num = 1;

    private StopwatchTimer timer;

    String path = "recording.wav";
    Microphone microphone = new Microphone(path);

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
    public void stop(ActionEvent e){
        microphone.stopRecording();
        timer.stopTimer();
        status.setText("Status: Stopped Recording....");
        //timer.cancel();
    }
}
