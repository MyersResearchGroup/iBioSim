/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu.stategraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.Timer;

/**
 *
 * @author ldmtwo
 */
public abstract class UpdateTimer {

    public static int UPDATE_DURATION = 1000;//millisec
    DecimalFormat df1 = new DecimalFormat("00.000");
    DecimalFormat df2 = new DecimalFormat("###,###,###,##0");
    long initialMemory = 0;
    Timer timer;

    public abstract int getNumStates();

    public abstract int getStackHeight();

    public abstract String getLabel();

    public UpdateTimer(int delay) {
        UPDATE_DURATION = delay;
        initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        timer = new Timer(delay, al);
        // timer.start();
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void print() {
        al.actionPerformed(null);
    }
    final ActionListener al = new ActionListener() {

        long lastTime = System.currentTimeMillis();
        final long startTime = lastTime;
        int lastStates = 0;

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                System.gc();
                int numStates = getNumStates();
                long now = System.currentTimeMillis();
                long totalMem, freeMem;
                totalMem = Runtime.getRuntime().totalMemory();
                freeMem = Runtime.getRuntime().freeMemory();
                long mem = (totalMem - freeMem - initialMemory) / 1024;
                int deltaStates = numStates - lastStates;
                long timeMS = now - startTime;
                double deltaTime = (UPDATE_DURATION / 1000f);
                long timeSec = (long) (timeMS / 1000f);
                long h = timeSec / 3600, m = (timeSec % 3600) / 60;
                double s = (timeMS / 1000f) - h * 3600 - m * 60;
                int statesPerSec = (int) (deltaStates / deltaTime);
                String timeStr = String.format("%d:%02d:%2s", h, m, df1.format(s));
                int bytesPerState = (int) (1024l * mem / numStates);
                System.out.printf("Free:%-10s Used: %-10s Total: %-10s Stack: %-7s %s states/sec  %s B/state  %s\n",
                        df2.format(freeMem / 1024 / 1024) + " mb",
                        df2.format(mem / 1024) + " mb",
                        df2.format(totalMem / 1024 / 1024) + " mb ("
                        + df2.format(totalMem / bytesPerState / 1000) + " K states max)",
                        df2.format(getStackHeight()) + "",
                        String.format("%5s   %5s  ", getLabel(),
                        "|S| =" + df2.format(numStates)) + "  " + df2.format(statesPerSec),
                        df2.format(bytesPerState),
                        timeStr);
                lastTime = System.currentTimeMillis();
                lastStates = numStates;
            } catch (Exception ex) {
//                        ex.printStackTrace();
            }
        }
    };
}
