package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;

public class Controller implements Initializable {
    @FXML private ChoiceBox <String> audioLine;
    String path = "recording.wav";
    Microphone microphone = new Microphone(path);
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        audioLine.getItems().addAll(microphone.getTargetLines().keySet());
        audioLine.setValue("Default");
        audioLine.setOnAction(this::changeLine);
    }
    public void changeLine(ActionEvent event){
        microphone.setTargetLine(audioLine.getValue());
    }

    public void start(ActionEvent e) throws LineUnavailableException {
        microphone.startRecording();
    }
    public void stop(ActionEvent e){
        microphone.stopRecording();
    }
}
