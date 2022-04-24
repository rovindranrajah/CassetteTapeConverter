package sample;

import it.sauronsoftware.jave.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


public class AudioUtils {
    public static MediaPlayer getRecording(File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        File recording = file;
        Media media = new Media(recording.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        return player;
    }

    public static void mergeTracks(String name, LinkedList<File> songs){
        int count=0;
        final int TIMEOUT = 10;


        while (true){
           if(songs.size() == 0) {
               break;
           }
           else if (songs.size() == 2){

               try {
                   AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File("System/splitted/" + songs.get(0).getName()));
                   AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File("System/splitted/" + songs.get(1).getName()));

                   AudioInputStream appendedFiles =
                           new AudioInputStream(
                                   new SequenceInputStream(clip1, clip2),
                                   clip1.getFormat(),
                                   clip1.getFrameLength() + clip2.getFrameLength());

                   AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, new File("System/splitted/" + name));
                   clip1.close();
                   clip2.close();
               } catch (Exception e) {
                   e.printStackTrace();
               }
               System.gc();
               try {
                   TimeUnit.MILLISECONDS.sleep(TIMEOUT);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               songs.poll().delete();
               songs.poll().delete();
           }
           else{
               try {
                   AudioInputStream clip1 = AudioSystem.getAudioInputStream(new File("System/splitted/" + songs.get(songs.size()-2).getName()));
                   AudioInputStream clip2 = AudioSystem.getAudioInputStream(new File("System/splitted/" + songs.get(songs.size()-1).getName()));

                   AudioInputStream appendedFiles =
                           new AudioInputStream(
                                   new SequenceInputStream(clip1, clip2),
                                   clip1.getFormat(),
                                   clip1.getFrameLength() + clip2.getFrameLength());

                   AudioSystem.write(appendedFiles, AudioFileFormat.Type.WAVE, new File("System/splitted/temp_" + count + ".wav"));
                   clip1.close();
                   clip2.close();
               } catch (Exception e) {
                   e.printStackTrace();
               }
               System.gc();
               try {
                   TimeUnit.MILLISECONDS.sleep(TIMEOUT);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               songs.pollLast().delete();
               songs.pollLast().delete();
               songs.addLast(new File("System/splitted/temp_" + count + ".wav"));
               count++;
           }

        }
    }

    public static void splitTrack(File file, double splitTime) throws IOException, UnsupportedAudioFileException {
        String name = file.getName().replaceFirst("[.][^.]+$", "");
        final int TIMEOUT = 10;
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        int bps = audioInputStream.getFormat().getSampleSizeInBits();
        int n = audioInputStream.getFormat().getChannels();
        float f = audioInputStream.getFormat().getSampleRate();
        double time = splitTime;
        //System.out.println(time);
        int length = (int)((f*n*(bps/8))*time);
        //System.out.println("Total: " + audioInputStream.getFrameLength()*audioInputStream.getFormat().getFrameSize());
       // System.out.println(f*n*(bps/8));
       // System.out.println(audioInputStream.getFormat().getFrameSize());
        byte[] b = new byte[length];
        audioInputStream.read(b);

        AudioInputStream stream = new AudioInputStream(
                new ByteArrayInputStream(b),
                new AudioFormat(f, bps, n, true, false), (length/audioInputStream.getFormat().getFrameSize()));
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File("System/splitted/" + name + "_1.wav"));

        //audioInputStream.read(b, 352800*5+44100, (int)audioInputStream.getFrameLength());
        int length2 = (int)audioInputStream.getFrameLength()*audioInputStream.getFormat().getFrameSize()-length;
        byte[] b2 = new byte[length2];
       // System.out.println(length2);
        audioInputStream.read(b2);
        AudioInputStream stream2 = new AudioInputStream(
                new ByteArrayInputStream(b2),
                new AudioFormat(f, bps, n, true, false), (length2/audioInputStream.getFormat().getFrameSize()));
        AudioSystem.write(stream2,AudioFileFormat.Type.WAVE, new File("System/splitted/" + name + "_2.wav"));
        audioInputStream.close();

        System.gc();
        try {
            TimeUnit.MILLISECONDS.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        file.delete();
    }
    public static float getDuration(File file) throws Exception {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = audioInputStream.getFormat();
        float durationInSeconds = audioInputStream.getFrameLength()/format.getFrameRate();
        //System.out.println(format.getFrameRate());
        //System.out.println("Filename: " +file.getName() + " FL: " + audioInputStream.getFrameLength() + " Seconds: " + durationInSeconds);
        return (durationInSeconds*1000);
    }

    /**
     * Get the sound pressure level in dB from the given sound buffer.
     *
     * @param samples
     * @return
     */
    public static double getSoundPressureLevel(final float[] samples) {
        double power = 0.0D;
        for (float element : samples) {
            power += element * element;
        }
        double value = Math.pow(power, 0.5);
        value = value / samples.length;
        double dB = 20.0 * Math.log10(value);
        return dB;
    }

    /**
     * Converts wav file to mp3.
     *
     * @param wav
     * @param mp3
     * @param bitRates
     * @param channels
     * @param sampleRate
     * @throws IllegalArgumentException
     * @throws InputFormatException
     * @throws EncoderException
     */
    public static void wav2mp3(String wav, String mp3, int bitRates, int channels, int sampleRate)
            throws IllegalArgumentException, InputFormatException, EncoderException {
        File source = new File(wav);
        File target = new File(mp3);
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(bitRates);
        audio.setChannels(channels);
        audio.setSamplingRate(sampleRate);
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        Encoder encoder = new Encoder();
        encoder.encode(source, target, attrs);
    }

    /**
     * Convert sound buffer to samples to measure the sound pressure level.
     * @param buf
     * @param dataLength
     * @return
     */
    public static float[] convertBytesToSamples(byte[] buf, int dataLength) {
        float[] samples = new float[buf.length / 2];
        // convert bytes to samples here
        for (int i = 0, s = 0; i < dataLength;) {
            int sample = 0;
            sample |= buf[i++] & 0xFF; // (reverse these two lines
            sample |= buf[i++] << 8; // if the format is big endian)
            // normalize to range of +/-1.0f
            samples[s++] = sample / 32768f;
        }
        return samples;
    }

    /**
     * Save bytes to new wav file
     * @param data
     * @param fileName
     */
    public static void saveBytesToFile(byte[] data, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Append bytes to existing wav file
     * @param data
     * @param fileName
     */
    public static File appendBytesToFile(byte[] data, String fileName) {
        String timestamp = "" + System.currentTimeMillis();
        File mainFile = new File(fileName);
        File tempFile1 = new File(Microphone.dir + "/temp_1_" + timestamp);
        File tempFile2 = new File(Microphone.dir + "/temp_2_" + timestamp);
        File combFile = new File(Microphone.dir + "/temp_c_" + timestamp);

        try {
            Files.copy(mainFile.toPath(), tempFile1.toPath(), StandardCopyOption.REPLACE_EXISTING);
            // Write data to a teporary wav file
            FileOutputStream fos = new FileOutputStream(tempFile2);
            fos.write(data);
            fos.flush();
            fos.close();

            AudioInputStream clip1 = AudioSystem.getAudioInputStream(tempFile1);
            AudioInputStream clip2 = AudioSystem.getAudioInputStream(tempFile2);
            SequenceInputStream seqIs = new SequenceInputStream(clip1, clip2);
            AudioInputStream appendedFiles = new AudioInputStream(
                    seqIs,
                    clip1.getFormat(),
                    clip1.getFrameLength() + clip2.getFrameLength());

            AudioSystem.write(appendedFiles,
                    AudioFileFormat.Type.WAVE,
                    combFile);
            clip1.close();
            clip2.close();
            appendedFiles.close();
            seqIs.close();
            tempFile1.deleteOnExit();
            tempFile2.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return combFile;
    }
}
