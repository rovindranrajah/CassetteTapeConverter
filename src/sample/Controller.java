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
import java.util.ResourceBundle;
import java.util.Vector;

public class Controller implements Initializable {
    @FXML private ChoiceBox <String> audioLine;

    private String [] lines = {"Microphone 1", "Microphone 2", "Microphone 3"};
    private TargetDataLine targetLine = AudioDevice();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Vector<String> dataLines = getLines();
        audioLine.getItems().addAll(dataLines);
        audioLine.setValue(dataLines.get(1));
    }

    public TargetDataLine AudioDevice(){
        TargetDataLine targetLine;
        try {
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

            DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, format);

            if(!AudioSystem.isLineSupported(dataInfo)){
                System.out.println("Not supported");
            }
            targetLine = (TargetDataLine) AudioSystem.getLine(dataInfo);
            return targetLine;
        }
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
    public void start(ActionEvent e) throws LineUnavailableException {
        System.out.println("Starting Recording");
        targetLine.open();
        targetLine.start();
        Thread audioRecorderThread = new Thread(){
            @Override
            public void run(){
                AudioInputStream recordingStream  = new AudioInputStream(targetLine);
                File outputFile = new File("record.wav");
                try{
                    AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, outputFile);
                }
                catch (IOException ex){
                    System.out.println(ex);
                }
            }
        };
        audioRecorderThread.start();
    }
    public void stop(ActionEvent e){
        System.out.println("Stopped Recording");
        targetLine.stop();
        targetLine.close();
    }

    public Vector<String> getLines() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        Vector<String> lineNames = new Vector<String>();
        for (Mixer.Info info: mixerInfos){
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getSourceLineInfo();
            //System.out.println (info.getName());
            for (Line.Info lineInfo:lineInfos){
                lineNames.add(info.getName());
                try {
                    Line line = m.getLine(lineInfo);
                    //System.out.println("\t-----" + line);
                }catch(Exception e){
                    System.out.println("Error");
                }
            }
            /*lineInfos = m.getTargetLineInfo();
            for (Line.Info lineInfo:lineInfos){
                //System.out.println (m+"---"+lineInfo);
                try {
                    Line line = m.getLine(lineInfo);
                    // System.out.println("\t-----" + line);
                }catch(Exception e){
                    System.out.println("Error");
                }
            }
            */
        }
        return lineNames;
    }
}
