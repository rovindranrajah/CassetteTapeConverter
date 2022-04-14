package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    @FXML private Button cancelButton, okayButton, visualAdjust;
    @FXML private TextField thresholdField;
    @FXML private TextField durationField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        thresholdField.setText(Double.toString(Processor.threshold));
        durationField.setText(Integer.toString(Microphone.duration));
        if(!Controller.recorded){
            visualAdjust.setDisable(true);
        }
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

    public void setVisualAdjust() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CassetteTapeConverter.class.getResource("VisualThreshold.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage saveWin = new Stage();
        saveWin.setTitle("Visual Threshold Adjust");
        saveWin.setScene(scene);
        saveWin.setResizable(false);
        VisualThresholdController.setThresholdField(thresholdField);
        saveWin.show();
    }


}
