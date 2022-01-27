package sample;

import it.sauronsoftware.jave.EncoderException;
import javafx.scene.control.ChoiceBox;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

//This class is used to set up the audio inputs, change the desired audio inputs, show the available audio inputs and also record and saves the file.


public class Microphone {
    public static final int CHANNEL = 2;
    public static final int BIT_RATE = 320000;
    public static final int SAMPLE_RATE = 44100;
    private final static int MAX_BUFFER_SIZE_IN_MB = 40; // Max buffer size in memory to avoid out of memory error
    private TargetDataLine targetLine = null;
    private HashMap<String, Line> targetLines = null;
    public static String dir;
    private String wavPath;
    private String mp3Path = "System/converted/recording.mp3";
    Thread graphThread;
    private boolean track_started=false;
    private boolean stop = false;

    public Microphone(){
        this.wavPath = "record.wav";
        this.dir = "System/rawFile";
        targetLines = getTargetLines();
        targetLine = (TargetDataLine) targetLines.get("Default");
    }

    public Microphone(String wavPath, String dir){
        this.wavPath = dir + "/" + wavPath;
        this.dir = dir;
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
        //System.out.println("Starting Recording");

        SwingWorkerRealTime swingWorkerRealTime = new SwingWorkerRealTime();
        targetLine.open();
        targetLine.start();
        track_started=true;
        graphThread = new Thread() {
            @Override
            public void run() {
                {
                    WaveWriter writer = null;
                    long counter = 0;
                    byte[] buf = new byte[88200];
                    double currentSPL = 0;
                    try {
                        writer = new WaveWriter(SAMPLE_RATE, 16, CHANNEL);
                        AudioUtils.saveBytesToFile(writer.getData(), wavPath);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    for (int b; (b = targetLine.read(buf, 0, buf.length)) > -1; ) {
                        try {
                            writer.write(buf, 0, b);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        float[] samples = AudioUtils.convertBytesToSamples(buf, b);
                        currentSPL = AudioUtils.getSoundPressureLevel(samples);
                        double threshold = Processor.threshold;
                        boolean silence = currentSPL < threshold;

                        if (silence) {
                            counter++;
                            if (counter >= 200) {
                                try {
                                    if (track_started) {
                                        File file = AudioUtils.appendBytesToFile(writer.getData(), wavPath);
                                        Files.copy(file.toPath(), new File(wavPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    } else {
                                        AudioUtils.saveBytesToFile(writer.getData(), wavPath);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                counter = 0;
                                targetLine.stop();
                                targetLine.close();
                                Thread processor = new Thread(new Processor());
                                processor.start();
                                return;
                            }
                        } else {
                            counter = 0;
                        }

                        if (stop) {
                            try {
                                if (track_started) {
                                    File file = AudioUtils.appendBytesToFile(writer.getData(), wavPath);
                                    Files.copy(file.toPath(), new File(wavPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                } else {
                                    AudioUtils.saveBytesToFile(writer.getData(), wavPath);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            counter = 0;
                            targetLine.stop();
                            targetLine.close();
                            return;
                        }

                        int bufferSize = 0;
                        try {
                            bufferSize = writer.getData().length;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        // Handles out of memory issue. Once the buffer is over MAX_BUFFER_SIZE_IN_MB MB it will append it to the file.
                        if (bufferSize > MAX_BUFFER_SIZE_IN_MB * 1024 * 1024) {
                            try {
                                if (track_started) {
                                    File file = AudioUtils.appendBytesToFile(writer.getData(), wavPath);
                                    Files.copy(file.toPath(), new File(wavPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
                                } else {
                                    AudioUtils.saveBytesToFile(writer.getData(), wavPath);
                                }
                                track_started = true;
//						System.out.println("Buffer Overflow : [" + wavPath + "]" + writer.getData().length);
                                writer = new WaveWriter(SAMPLE_RATE, 16, CHANNEL);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        double displaySPL = currentSPL;


                        swingWorkerRealTime.go(currentSPL);


                    }

                    for (int b; (b = targetLine.read(buf, 0, buf.length)) > -1; ) {


                        float[] samples = AudioUtils.convertBytesToSamples(buf, b);
                        currentSPL = AudioUtils.getSoundPressureLevel(samples);
                        double threshold = Processor.threshold;
                        boolean silence = currentSPL < threshold;

                        swingWorkerRealTime.go(currentSPL);
                    }
                }
            }
        };

        graphThread.start();
        //audioRecorderThread.start();

    }

    public void stopRecording(){
        //System.out.println("Stopped Recording");
        //graphThread.interrupt();
        stop=true;
        targetLine.stop();
        targetLine.close();

        try {
            AudioUtils.wav2mp3(wavPath, mp3Path, BIT_RATE, CHANNEL, SAMPLE_RATE);
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        new Thread(new Processor()).start();
    }
}
