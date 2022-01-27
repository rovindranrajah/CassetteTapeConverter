package sample;

import it.sauronsoftware.jave.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;


public class AudioUtils {
    public static MediaPlayer getRecording(File file) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        File recording = file;
        /*audioStream = AudioSystem.getAudioInputStream(recording);
        clip = AudioSystem.getClip();
        clip.open(audioStream);*/
        Media media = new Media(recording.toURI().toString());
        MediaPlayer player = new MediaPlayer(media);
        return player;
    }

    public static void mergeTracks(String name, LinkedList<File> songs){
        System.out.println(name);
        LinkedList<File> tempSongs = new LinkedList<File>();
       while (true){
           if(songs.size() == 0) {
               break;
           }
           try {
                int temp;

                tempSongs.add(songs.poll());
                tempSongs.add(songs.poll());
                FileInputStream fistream1 = new FileInputStream("System/splitted/" + tempSongs.peekFirst().getName());
                FileInputStream fistream2 = new FileInputStream("System/splitted/" + tempSongs.peekLast().getName());
                SequenceInputStream sistream = new SequenceInputStream(fistream1, fistream2);
                FileOutputStream fostream = new FileOutputStream("System/splitted/" + name);


                while((temp = sistream.read()) != -1)
                {
                    fostream.write(temp);
                }
                fostream.close();
                sistream.close();
                fistream1.close();
                fistream2.close();
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

           tempSongs.poll();

           //System.out.println(file.delete());
           /*file = new File("System/splitted/" + tempSongs.poll());
           file.delete();*/

        }
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

    public static byte[] convertSamplesToBytes(float[] samples) {
        final byte[] byteBuffer = new byte[samples.length * 2];
        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length;) {
            final int x = (int) (samples[bufferIndex++] * 32767.0);
            byteBuffer[i++] = (byte) x;
            byteBuffer[i++] = (byte) (x >>> 8);
        }
        return byteBuffer;
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
