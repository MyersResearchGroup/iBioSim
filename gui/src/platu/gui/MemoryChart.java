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
import platu.stategraph.StateGraph;

/**
 *
 * @author ldtwo
 */
public class MemoryChart extends Thread {

    JFrame f = new JFrame();
    XYSeries free = new XYSeries("Free");
    XYSeries used = new XYSeries("Used");
    XYSeries total = new XYSeries("Total");
    XYSeries max = new XYSeries("Max");
    LPN sg;
    public boolean running = true;

//    public MemoryChart(StateGraph sg) {
//        this.sg = sg;
//    }
    public MemoryChart(LPN sg) {
        this.sg = sg;
    }

    @Override
    @SuppressWarnings("SleepWhileHoldingLock")
    public void run() {
        long init = System.currentTimeMillis() / 1000;
        long unit = 1000;
        f.setSize(600, 400);
        f.show();
        f.setLocation(0, 0);
//        f.setTitle(sg.label);
        while (running) {
            try {
                //System.gc();
                long time = System.currentTimeMillis() / 1000 - init;
                free.add(time, Runtime.getRuntime().freeMemory() / 1024 / 1024);
                long mem=(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
                  Main.MAX_MEM = Main.MAX_MEM > mem ? Main.MAX_MEM : mem;
                used.add(time, mem / 1024);
                total.add(time, Runtime.getRuntime().totalMemory() / 1024 / 1024);
                max.add(time, Runtime.getRuntime().maxMemory() / 1024 / 1024);
                ChartPanel pan = new ChartPanel(getChart());

                f.setContentPane(pan);

                f.validate();
//                f.repaint();
//                yield();
                sleep(unit);
                unit += (time*10);
            } catch (InterruptedException ex) {
                Logger.getLogger(MemoryChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Runnable runnable = new Runnable() {

            @Override
            @SuppressWarnings("SleepWhileHoldingLock")
            public void run() {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss.SSSS");
                String date = df.format(new Date());
//                try {
//                    ChartUtilities.saveChartAsJPEG(new File(
//                            new File(TimedStateGraph.LPN_PATH+"\\"+Main.RESULT_FOLDER).getAbsolutePath()
//                            + "\\" + sg.label + "_memory_" + date + ".jpg"), getChart(), 1000, 900);
//                } catch (Exception e) {
//                    System.out.println("Problem occurred creating memory chart.");
//                }
                for (long x = Main.GRAPH_KEEP_ALIVE_TIME / 1000; x >= 0; x--) {
                    try {
                        f.setTitle("Finished - closing in " + x + " sec");
                        Thread.sleep(1000);
                        f.validate();
                        f.repaint();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MemoryChart.class.getName()).log(Level.SEVERE, null, ex);
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
        dataset.addSeries(free);
        dataset.addSeries(used);
        dataset.addSeries(total);
        dataset.addSeries(max);
        //         Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart("Memory", "Seconds", "Kbytes", dataset, PlotOrientation.VERTICAL, true, true, false);
        return chart;
    }
}
