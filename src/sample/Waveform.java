package sample;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Waveform {

    private static JPanel graphPanel;
    static BufferedImage img;
    static int boxWidth = 4;
    static Dimension size = new Dimension(1150, 600);
    private static boolean newTrack = true;

    /*public static void main(String[] args) {
        JFrame frame = new JFrame();


        frame.setSize(1500, 650);
        frame.setVisible(true);

        frame.add(getPanel());

    }*/
    public static JPanel getPanel(){
        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if(img != null) {
                    g.drawImage(img, 1, 1, img.getWidth(), img.getHeight(), null);
                }
            }
        };
        graphPanel.setBorder(new LineBorder(Color.BLACK));
        graphPanel.setBounds(0, 111, 510, 107);
        loadImage();
        return graphPanel;
    }
    // draw the image
    static void drawImage(float[] samples) {
        Graphics2D g2d = img.createGraphics();

        int numSubsets = size.width / boxWidth;
        int subsetLength = samples.length / numSubsets;

        float[] subsets = new float[numSubsets];

        // find average(abs) of each box subset
        int s = 0;
        for(int i = 0; i < subsets.length; i++) {

            double sum = 0;
            for(int k = 0; k < subsetLength; k++) {
                sum += Math.abs(samples[s++]);
            }

            subsets[i] = (float)(sum / subsetLength);
        }

        // find the peak so the waveform can be normalized
        // to the height of the image
        float normal = 0;
        for(float sample : subsets) {
            if(sample > normal)
                normal = sample;
        }

        // normalize and scale
        normal = 32768.0f / normal;
        for(int i = 0; i < subsets.length; i++) {
            subsets[i] *= normal;
            subsets[i] = (subsets[i] / 32768.0f) * (size.height / 2);
        }

        if (newTrack == true){
            g2d.setColor(Color.WHITE);
        } else {
            g2d.setColor(Color.GRAY);
        }

        // convert to image coords and do actual drawing
        for(int i = 0; i < subsets.length; i++) {
            int sample = (int) subsets[i];

            int posY = (size.height / 2) - sample;
            int negY = (size.height / 2) + sample;

            int x = i * boxWidth;

            if (newTrack == true){

                if (boxWidth == 1) {
                    g2d.drawLine(x, posY, x, negY);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(x + 1, posY + 1, boxWidth - 1, negY - posY - 1);
                    g2d.setColor(Color.WHITE);
                    g2d.drawRect(x, posY, boxWidth, negY - posY);
                    newTrack = false;
                }
            } else {
                if (boxWidth == 1) {
                    g2d.drawLine(x, posY, x, negY);
                } else {
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(x + 1, posY + 1, boxWidth - 1, negY - posY - 1);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.drawRect(x, posY, boxWidth, negY - posY);
                }
            }
        }

        g2d.dispose();
        graphPanel.repaint();
        graphPanel.requestFocusInWindow();
    }

    // Load the track waveform
    static void loadImage() {
        //System.out.println("Load");
        //String audioFile = "";
        File file = new File("System/rawFile/recording.wav");
        //System.out.println(file);
        float[] samples;

        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioFormat fmt = in.getFormat();

            if(fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                throw new UnsupportedAudioFileException("unsigned");
            }

            boolean big = fmt.isBigEndian();
            int chans = fmt.getChannels();
            int bits = fmt.getSampleSizeInBits();
            int bytes = bits + 7 >> 3;

            int frameLength = (int)in.getFrameLength();
            int bufferLength = chans * bytes * 1024;

            samples = new float[frameLength];
            byte[] buf = new byte[bufferLength];

            int i = 0;
            int bRead;
            while((bRead = in.read(buf)) > -1) {

                for(int b = 0; b < bRead;) {
                    double sum = 0;

                    // (sums to mono if multiple channels)
                    for(int c = 0; c < chans; c++) {
                        if(bytes == 1) {
                            sum += buf[b++] << 8;

                        } else {
                            int sample = 0;

                            // (quantizes to 16-bit)
                            if(big) {
                                sample |= (buf[b++] & 0xFF) << 8;
                                sample |= (buf[b++] & 0xFF);
                                b += bytes - 2;
                            } else {
                                b += bytes - 2;
                                sample |= (buf[b++] & 0xFF);
                                sample |= (buf[b++] & 0xFF) << 8;
                            }

                            final int sign = 1 << 15;
                            final int mask = -1 << 16;
                            if((sample & sign) == sign) {
                                sample |= mask;
                            }

                            sum += sample;
                        }
                    }

                    samples[i++] = (float)(sum / chans);
                }
            }

        } catch(Exception e) {
            return;
        }

        if(img == null) {
            img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        }

        drawImage(samples);
    }
}
