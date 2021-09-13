package sample;

import javafx.scene.control.ChoiceBox;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

//This class is used to set up the audio inputs, change the desire audio inputs, show the available audio inputs and also record and saves the file.


public class Microphone {
    private TargetDataLine targetLine = null;
    private HashMap<String, Line> targetLines = null;
    private String path;

    public Microphone(String path){
        this.path = path;
        targetLines = getTargetLines();
        targetLine = (TargetDataLine) targetLines.get("Default");
    }
    public HashMap<String, Line> getTargetLines() {
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        HashMap<String,Line> targetLines = new HashMap<String, Line>();

        for (Mixer.Info info: mixerInfos){
            Mixer m = AudioSystem.getMixer(info);
            Line.Info[] lineInfos = m.getTargetLineInfo();
            if(lineInfos.length>=1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)){//Only prints out info is it is a Microphone
                //System.out.println("Line Name2 (Mixer.Info): " + info.getName());//The name of the AudioDevice

                //System.out.println("Line Description: " + info.getDescription());//The type of audio device
                for (Line.Info lineInfo:lineInfos){
                    // System.out.println("Line");
                    //System.out.println ("\t"+"---"+lineInfo);
                    Line line = null;
                    try {
                        line = m.getLine(lineInfo);
                        if(info.getName().equals("Primary Sound Capture Driver")){
                            targetLines.put("Default", line);
                        }
                        else{
                            targetLines.put(info.getName(), line);
                        }
                        //System.out.println("Target Lines" + m.getTargetLines());
                    } catch (LineUnavailableException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();

                    }
                    //System.out.println("\t(Mixer)-----"+line);
                }
            }
        }
        return targetLines;
    }

    public void setTargetLine(String line) {
        targetLine = (TargetDataLine) targetLines.get(line);
    }

    public void startRecording() throws LineUnavailableException {
        System.out.println("Starting Recording");
        targetLine.open();
        targetLine.start();
        Thread audioRecorderThread = new Thread(){
            @Override
            public void run(){
                AudioInputStream recordingStream  = new AudioInputStream(targetLine);
                File outputFile = new File(path);
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

    public void stopRecording(){
        System.out.println("Stopped Recording");
        targetLine.stop();
        targetLine.close();
    }
}
