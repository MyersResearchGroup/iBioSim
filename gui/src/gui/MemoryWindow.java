/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import platu.main.Main;

/**
 *
 * @author ldtwo
 */
public class MemoryWindow extends Thread {

    static JFrame f;

    static {
    }
    XYSeries free = new XYSeries("Free");
    XYSeries used = new XYSeries("Used");
    XYSeries total = new XYSeries("Total");
    XYSeries max = new XYSeries("Max");
    public boolean running = true;

    public MemoryWindow() {
        super(MemoryWindow.class.getSimpleName());
    }

    void resize() {
        free.remove(0);
        used.remove(0);
        total.remove(0);
        max.remove(0);

    }
    long init = System.currentTimeMillis() / 1000;
 

    @Override
    @SuppressWarnings("deprecation")
    public void run() {
        try { 
            EventQueue.invokeLater(new Thread(new Runnable() {

                JFrame frame;
                @Override
                public void run() {
                    frame = new JFrame();
                    f = frame;
                      ChartPanel pan = new ChartPanel(getChart());
                    f.setContentPane(pan);
                }
            }));
           
            long unit = 1000;
            while(f==null)Thread.sleep(100);
            f.setSize(400, 700);
            f.show();
            f.setLocation(0, 0);
            f.setTitle("MemoryChart");
            int itter = 0;
            while (running) {
                itter++;
                try {
                if (itter > 35) {
                    resize();
                }
                    //System.gc();
                    Runnable runnable = new Runnable() {

                        public void run() {
                            addToSeries();
                        }


                    };
                    Main.exec.submit(runnable).get();

//                f.repaint();
//                yield();
                    sleep(unit);
//                unit += 100;
//                unit=(long) (unit * 1.1);
                } catch (Exception ex) {
                  //  Logger.getLogger(MemoryWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void chartVisual(JFreeChart chart) {
        chart.setBackgroundPaint(Color.BLACK);
        chart.setBorderPaint(Color.green);
        chart.getXYPlot().setBackgroundPaint(Color.black);
        chart.getXYPlot().getDomainAxis().setAxisLinePaint(Color.GREEN);
        chart.getXYPlot().getDomainAxis().setLabelPaint(Color.GREEN);
        chart.getXYPlot().getDomainAxis().setTickLabelPaint(Color.GREEN);
        chart.getXYPlot().getRangeAxis().setAxisLinePaint(Color.GREEN);
        chart.getXYPlot().getRangeAxis().setLabelPaint(Color.GREEN);
        chart.getXYPlot().getRangeAxis().setTickLabelPaint(Color.GREEN);
        chart.getXYPlot().setRangeZeroBaselinePaint(Color.green);
        chart.getXYPlot().setRangeGridlinePaint(Color.green);
        chart.getXYPlot().setRangeCrosshairPaint(Color.green);
        chart.getXYPlot().setDomainZeroBaselinePaint(Color.green);
        chart.getXYPlot().setDomainGridlinePaint(Color.green);
        chart.getXYPlot().setDomainCrosshairPaint(Color.green);
        chart.getLegend().setBackgroundPaint(Color.black);
        chart.getLegend().setItemPaint(Color.green);
        //                    chart.getLegend().setBackgroundPaint(Color.green);
        chart.getTitle().setPaint(Color.green);
        chart.setTextAntiAlias(true);
    }

    JFreeChart getChart() {
        //         Add the series to your data set
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(free);
        dataset.addSeries(used);
        dataset.addSeries(total);
        dataset.addSeries(max);
        //         Generate the graph

        JFreeChart chart = ChartFactory.createXYLineChart("Memory", "Seconds", "Mbytes", dataset, PlotOrientation.VERTICAL, true, true, false);
        return chart;
    }
     synchronized  private void addToSeries() {
                            try {
                                ChartPanel pan = new ChartPanel(getChart());
                                long time = System.currentTimeMillis() / 1000 - init;
                                free.add(time, Runtime.getRuntime().freeMemory() / 1024 / 1024);
                                long mem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024;
                                Main.MAX_MEM = Main.MAX_MEM > mem ? Main.MAX_MEM : mem;
                                used.add(time, mem / 1024);
                                total.add(time, Runtime.getRuntime().totalMemory() / 1024 / 1024);
                                max.add(time, Runtime.getRuntime().maxMemory() / 1024 / 1024);
                                pan.setBackground(Color.black);
                                pan.setForeground(Color.green);
                                JFreeChart chart = getChart();
                                chartVisual(chart);
                                pan.setChart(chart);
                                f.setContentPane(pan);
                                f.validate();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
}
