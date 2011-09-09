/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package platu.project;

/**
 *
 * @author ldmtwo
 */
public class PrintStackTrace extends Exception{

    public PrintStackTrace() {
        this.printStackTrace();
    }

    public PrintStackTrace(String s) {
        super(s);
        this.printStackTrace();
    }
}
