package sample;

import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.List;

import java.awt.*;
import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

/**
 * Creates a real-time chart using SwingWorker
 */
public class SwingWorkerRealTime {
    JFrame frame;
    MySwingWorker mySwingWorker;
    SwingWrapper<XYChart> sw;
    XYChart chart;
    boolean played = false;
    double cSPL;

    public void go(double SPL) {

        if(!played){
            played = true;
            cSPL = SPL;

            // Create Chart
            chart = QuickChart.getChart("dB Visualization", "dB", " ", "randomWalk", new double[] { 0 }, new double[] { 0 });
            chart.getStyler().setLegendVisible(false);
            chart.getStyler().setXAxisTicksVisible(false);

            // Show it
            sw = new SwingWrapper<XYChart>(chart);
            //sw.displayChart();


            frame = sw.displayChart();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
            Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
            int x = (int) rect.getMaxX() - frame.getWidth();
            int y = 0;
            frame.setLocation(x, y);
            javax.swing.SwingUtilities.invokeLater(
                    ()->frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE)
            );

            mySwingWorker = new MySwingWorker();
            mySwingWorker.set_dSPL(cSPL);

            mySwingWorker.execute();
        }

        else{
            cSPL = SPL;

            mySwingWorker.set_dSPL(cSPL);
        }
    }

    public double getSPL(){
        return cSPL;
    }

    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    private class MySwingWorker extends SwingWorker<Boolean, double[]> {

        LinkedList<Double> fifo = new LinkedList<Double>();
        double dSPL;

        public void set_dSPL(double eSPL){
            dSPL = eSPL;
        }

        public double get_dSPL(){
            return dSPL;
        }

        public MySwingWorker() {
            fifo.add(0.0);
        }

        @Override
        protected Boolean doInBackground() throws Exception {

            while (!isCancelled()) {
//    	System.out.println(get_dSPL());

                fifo.add(getSPL());

//     	fifo.add(fifo.get(fifo.size() - 1) + Math.random() - .5);
                if (fifo.size() > 500) {
                    fifo.removeFirst();
                }

                double[] array = new double[fifo.size()];
                for (int i = 0; i < fifo.size(); i++) {
                    array[i] = fifo.get(i);

                }
                publish(array);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // eat it. caught when interrupt is called
                    System.out.println("MySwingWorker shut down.");
                }

            }

            return true;
        }

        @Override
        protected void process(List<double[]> chunks) {

            double[] mostRecentDataSet = chunks.get(chunks.size() - 1);

            chart.updateXYSeries("randomWalk", null, mostRecentDataSet, null);
            sw.repaintChart();

            long start = System.currentTimeMillis();
            long duration = System.currentTimeMillis() - start;
            try {
                Thread.sleep(40 - duration); // 40 ms ==> 25fps
                // Thread.sleep(400 - duration); // 40 ms ==> 2.5fps
            } catch (InterruptedException e) {
            }

        }

    }
}

/*public void SoundGraph() {
	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	setBounds(100, 100, 850, 650);
	contentPane = new JPanel();

}*/

