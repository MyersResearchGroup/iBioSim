/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package backend.verification.platu.project;

/**
 *
 * @author ldmtwo
 */
public class PrintStackTrace extends Exception{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PrintStackTrace() {
        this.printStackTrace();
    }

    public PrintStackTrace(String s) {
        super(s);
        this.printStackTrace();
    }
}
