package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class VisualThresholdController implements Initializable {
    private static TextField thresholdField;
    @FXML
    Slider slider;
    @FXML
    SwingNode graphArea;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            splitBySilentArea();
        } catch (IOException e) {
            e.printStackTrace();
        }


        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setThreshold();
            }
        });
        createAndSetSwingDrawingPanel(graphArea);
        graphArea.setVisible(true);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int run = 5;
            @Override
            public void run() {
                if(run>0){
                    test();
                    run--;
                }else{
                    cancel();
                }

                //System.out.println("Running");
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 1000);
    }
    public void test(){
        Stage stage = (Stage)slider.getScene().getWindow();
        stage.setX(stage.getX()-1);
        stage.setX(stage.getX()+1);
    }
    public static void setThresholdField(TextField thresholdField2){
       thresholdField = thresholdField2;
    }
    private void splitBySilentArea() throws IOException {
        AudioInputStream din = null;
        double max,min;
        LinkedList<Double> spl = new LinkedList<Double>();

        try {
            din = AudioSystem.getAudioInputStream(new File("System/rawFile/recording.wav"));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }

        double currentSPL = 0;
        byte[] buf = new byte[(int)din.getFormat().getFrameRate() * din.getFormat().getChannels()];

        for (int b; (b = din.read(buf, 0, buf.length)) > -1;) {


            float[] samples = AudioUtils.convertBytesToSamples(buf, b);

            currentSPL = AudioUtils.getSoundPressureLevel(samples);

            spl.add(currentSPL);
            //System.out.println(currentSPL);

        }
        Collections.sort(spl);
        /*System.out.println(spl.getFirst());
        System.out.println(spl.getLast());*/

        slider.setMax(spl.getLast());
        slider.setMin(spl.getFirst());
        slider.setValue(Processor.threshold);
        din.close();
    }

    public void setThreshold(){
        Processor.threshold = slider.getValue();
        thresholdField.setText(Double.toString(Processor.threshold));
    }
    public void createAndSetSwingDrawingPanel(final SwingNode swingNode) {
        JPanel panel = new Waveform().getPanel();

        swingNode.setContent(panel);
    }
}
