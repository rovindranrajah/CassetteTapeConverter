package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    @FXML private Button cancelButton, okayButton;
    @FXML private TextField thresholdField;
    @FXML private TextField durationField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        thresholdField.setText(Double.toString(Processor.threshold));
        durationField.setText(Integer.toString(Microphone.duration));
    }

    public void okay(){
        Processor.threshold = Double.parseDouble(thresholdField.getText());
        Microphone.duration = Integer.parseInt(durationField.getText());
        Stage stage = (Stage) okayButton.getScene().getWindow();
        stage.close();
    }

    public void cancel(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }


}
