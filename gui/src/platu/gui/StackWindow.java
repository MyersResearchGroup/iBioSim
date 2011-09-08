/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StackWindow.java
 *
 * Created on Mar 27, 2011, 4:18:21 PM
 */

package platu.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

/**
 *
 * @author ldmtwo
 */
public class StackWindow extends Thread {
final static JFrame f=new JFrame(StackWindow.class.getSimpleName());
    /** Creates new form StackWindow */
    public StackWindow() {
        super("JVM Stack");
        initComponents();

        Timer t=new Timer(3000,new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                jTabbedPane1.removeAll();
              Map<Thread,StackTraceElement[]> traces= Thread.getAllStackTraces();
//              System.out.println(traces);
              Component main=null;
              for(Thread t:traces.keySet()){
                  JPanel p=new JPanel(new GridLayout());
                  String str=t.toString()+"\n"+
                          arrayToString(traces.get(t));
                  JTextArea tex=new JTextArea(str);
                  p.add(tex);
                  if(t.getName().equals("main")){
                      main=p;
                  }
                  jTabbedPane1.add(t.getName(),p);
              }
             jTabbedPane1.validate();
             if(main!=null)
             jTabbedPane1.setSelectedComponent(main);
//             pack();
            }
        });
        t.start();
        f.setSize(800, 400);
        f.show();
       f. setLocation(410, 0);
    }
String arrayToString(Object[] objs){
    String ret="";
    for(Object o:objs)ret=o+"\n"+ret;
    return ret;
}

    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTabbedPane1 = new javax.swing.JTabbedPane();

        f.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        f.getContentPane().setLayout(new java.awt.GridLayout(1, 0));

       jScrollPane1.setViewportView(jTabbedPane1);

        f.getContentPane().add(jScrollPane1);

        f.pack();
    }

    /**
    * @param args the command line arguments
    */
 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
