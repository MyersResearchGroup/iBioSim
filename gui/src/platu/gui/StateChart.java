/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.gui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import platu.lpn.LPN;
import platu.main.Main;

/**
 *
 * @author ldtwo
 */
public class StateChart extends Thread {

    JFrame f = new JFrame();
    XYSeries states = new XYSeries("States");
    XYSeries stateTrans = new XYSeries("StateTrans");
    XYSeries level = new XYSeries("Recursion depth");
    LPN sg;
    public boolean running = true;

    public StateChart(LPN sg) {
        this.sg = sg;
    }

    @Override
    public void run() {
        long init = System.currentTimeMillis() / 1000;
        long unit = 1;
        f.setSize(600, 400);
        f.show();
        f.setLocation(600, 0);
        states.add(0, 0);
        stateTrans.add(0, 0);
        level.add(0, 0);
//        f.setTitle(sg.label);
        while (running) {
            try {
                sleep(unit * 1000);
                long time = System.currentTimeMillis() / 1000 - init;
                //states.add(time, sg.size());
                //stateTrans.add(time, sg.stateTrans.size());
//                level.add(time, sg.stackHeight);


                ChartPanel pan = new ChartPanel(getChart());

                f.setContentPane(pan);

                f.validate();
//                f.repaint();
//                yield();
                unit += (time*0.01);
            } catch (InterruptedException ex) {
                Logger.getLogger(StateChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Runnable runnable = new Runnable() {

            public void run() {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss.SSSS");
                String date = df.format(new Date());
//                try {
//                    ChartUtilities.saveChartAsJPEG(new File(
//                            new File(TimedStateGraph.LPN_PATH+"\\"+Main.RESULT_FOLDER).getAbsolutePath()
//                            + "\\" + sg.label + "_states_" + date + ".jpg"), getChart(), 1000, 900);
//                } catch (Exception e) {
//                    System.out.println("Problem occurred creating state chart.");
//                }
                for (long x = Main.GRAPH_KEEP_ALIVE_TIME / 1000; x >= 0; x--) {
                    try {
                        f.setTitle("Finished - closing in " + x + " sec");
                        Thread.sleep(1000);
                        f.validate();
                        f.repaint();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(StateChart.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                f.hide();
            }
        };
        new Thread(runnable).start();
    }

    JFreeChart getChart() {
        //         Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(states);
        dataset.addSeries(stateTrans);
        dataset.addSeries(level);
        //         Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart("States", "Seconds", "#", dataset,
                PlotOrientation.VERTICAL, true, true, false);
        return chart;
    }
}

