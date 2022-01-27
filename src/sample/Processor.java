package sample;

import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.InputFormatException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Processor implements Runnable{
    static int trackCount;
    private final static String DEFAULT_WAV_FILE2 = "System/rawFile/recording.wav";
    private final static String DEFAULT_TRACK_FILE = "TRACK";
    private final static String DEFAULT_TRACK_EXT = "mp3";
    private final static String OUTPUT_FOLDER = "System/splitted";
    private final static int SAMPLE_BITS = 16; // Sample Bits
    private final static int MAX_BUFFER_SIZE_IN_MB = 40;
    public static double threshold = -72;
    public static int duration = 6;

    Processor(){}
    private void splitBySilentArea(AudioFormat targetFormat, AudioInputStream din)
            throws IOException, LineUnavailableException {

        trackCount = 1;
        int MULTIPLIER=1;
        long counter = 0;
        double currentSPL = 0;
        byte[] buf = new byte[88200];
        boolean noise = true;
        boolean track_started = false;

        WaveWriter writer = null;
        try {
            writer = new WaveWriter(Microphone.SAMPLE_RATE, SAMPLE_BITS, Microphone.CHANNEL);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (int b; (b = din.read(buf, 0, buf.length)) > -1;) {

//				System.out.println("Frame length: " + din.getFrameLength());
//				System.out.println("Available frame: " + din.available());


            int maximum = (int) din.available();

//				System.out.println("Maximum: " + maximum);

            //updateBar((float)maximum,(float)din.available());

            boolean played2 = true;

            float[] samples = AudioUtils.convertBytesToSamples(buf, b);

            currentSPL = AudioUtils.getSoundPressureLevel(samples);

            boolean silence = currentSPL < threshold;

            if (silence) {
                counter++;
                if (counter >= duration * MULTIPLIER) {
                    if (!noise) {
                        String trackName = saveTrack(writer, trackCount, track_started, true);
                        track_started = false;

                        trackCount++;
                        writer = new WaveWriter(Microphone.SAMPLE_RATE, SAMPLE_BITS, Microphone.CHANNEL);
                    }
                    else {
                    }
                    counter = 0;
                    noise = true;
                }
                else {
                    writer.write(buf, 0, b);
                }
            }
            else {
                writer.write(buf, 0, b);
                counter = 0;
                noise = false;
            }


            int bufferSize = 0;
            try {
                bufferSize = writer.getData().length;
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Handles out of memory issue. Once the buffer is over MAX_BUFFER_SIZE_IN_MB MB it will append it to the file.
            if (bufferSize > MAX_BUFFER_SIZE_IN_MB * 1024 * 1024) {
                String trackName = saveTrack(writer, trackCount, track_started, false);
//					System.out.println("Buffer Overflow : [" + trackName + "]" + bufferSize);
                track_started = true;
                writer = new WaveWriter(Microphone.SAMPLE_RATE, SAMPLE_BITS, Microphone.CHANNEL);
            }

//				audioSplitter.setStatus("Processing ... " + currentSPL + " dB");


        }
        din.close();

        // Process last track
        if (!noise) {
            String trackName = saveTrack(writer, trackCount, track_started, true);
            track_started = false;

        } else {
            File wavTemp = new File("System/" + DEFAULT_TRACK_FILE + "_" + trackCount + ".wav");
            if(wavTemp.exists()) {
                wavTemp.delete();
            }
            //System.out.println("Noise...");
        }

    }

    private String saveTrack(WaveWriter currentWriter, int index, boolean append, boolean complete) {

        String trackName = DEFAULT_TRACK_FILE + "_" + index + "." + DEFAULT_TRACK_EXT;

        String wavTemp = "System/" + DEFAULT_TRACK_FILE + "_" + index + ".wav";

        try {
            File tempFile = new File(wavTemp);
            if (append) {
                File temp = AudioUtils.appendBytesToFile(currentWriter.getData(), wavTemp);
                Files.copy(temp.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } else {
                AudioUtils.saveBytesToFile(currentWriter.getData(), wavTemp);
            }
            if (complete) {
                AudioUtils.wav2mp3(wavTemp, OUTPUT_FOLDER + File.separator + trackName, Microphone.BIT_RATE, Microphone.CHANNEL, Microphone.SAMPLE_RATE);
                tempFile.deleteOnExit();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InputFormatException e) {
            e.printStackTrace();
        } catch (EncoderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trackName;
    }

    @Override
    public void run() {
//			System.out.println("Started Processing....");
        try {

            File file = new File(DEFAULT_WAV_FILE2);
//				File file = new File("C:\Users\User\Documents\Work\FYP\Tape Converter v2\Tape Converter v2\Full_Audio.wav");
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioInputStream din = null;
//				System.out.println(in.getFrameLength());
            if (in != null) {
                AudioFormat baseFormat = in.getFormat();
                AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(), false);
                din = AudioSystem.getAudioInputStream(decodedFormat, in);
                splitBySilentArea(decodedFormat, din);

                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//		System.out.println("Finished Processing....");

        }


}


