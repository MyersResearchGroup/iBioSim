/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package platu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import platu.Main;

/**Keep this file for logging errors! Throw this if you need to write a file,
 * save the stack trace, or save copies of files related to the error.
 */
public class CriticalError extends Error {

    private static final long serialVersionUID = 98273659823675L;

    /**
     * Keep this file for logging errors! Throw this if you need to write a file,
 * save the stack trace, or save copies of files related to the error.
     * @param e Exception, Error, or Throwable
     */
    CriticalError(Throwable e) {
        super(e);
        try {
            File f = new File("Critical_error.log");
            FileOutputStream os = new FileOutputStream(f, true);

            os.write(("\n==================================================\n"
                    + "   \t" + new Date() + "\n"
                    + "--------------------------------------------------\n").getBytes());
            if (e instanceof Exception) {
                e.printStackTrace();
                System.err.println("AN EXCEPTION OCCURRED");
                os.write(("AN UNCAUGHT EXCEPTION OCCURRED" + "\n").getBytes());
            } else if (e instanceof RuntimeException) {
                System.err.println("A RUNTIME EXCEPTION OCCURRED");
                os.write(("A CRITICAL RUNTIME EXCEPTION OCCURRED" + "\n").getBytes());
            } else if (e instanceof Error) {
                System.err.println("AN ERROR OCCURRED");
                os.write(("A CRITICAL ERROR OCCURRED" + "\n").getBytes());
            } else {
                System.err.println("A THROWABLE OCCURRED");
                os.write(("A UNCAUGHT THROWABLE OCCURRED" + "\n").getBytes());
            }
            System.err.println("   " + e.toString());
            os.write(("   " + e.toString() + "\n").getBytes());
            System.err.println("MESSAGE: " + e.getMessage());
            os.write(("MESSAGE: " + e.getMessage() + "\n").getBytes());
            System.err.println("CAUSE: " + e.getCause());
            os.write(("CAUSE: " + e.getCause() + "\n").getBytes());
            StackTraceElement[] elements = e.getStackTrace();
            System.err.println("STACK HEIGHT: " + elements.length);
            os.write(("STACK HEIGHT: " + elements.length + "\n").getBytes());

            for (StackTraceElement el : elements) {
                os.write((el.toString() + "\n").getBytes());
            }
            e.printStackTrace(new PrintStream(os));
            System.err.println("STACK TRACE WAS WRITTEN TO:\n" + f.getAbsolutePath());
        } catch (Exception ex2) {
            ex2.printStackTrace();
        }
    }

    /**
     *
     * Keep this file for logging errors! Throw this if you need to write a file,
 * save the stack trace, or save copies of files related to the error.
     * @param e Exception, Error, or Throwable
     * @param f1 Save a copy of this related file in its current state.
     * @param f2 Save a copy of this related file in its current state.
     */
    public CriticalError(Throwable e, File f1, File f2) {
        this(e);
        try {
            File fa = new File("Critical_error_file1.txt");
            File fb = new File("Critical_error_file2.txt");
            Main.copyFile(f1, fa);
            Main.copyFile(f2, fb);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Keep this file for logging errors! Throw this if you need to write a file,
 * save the stack trace, or save copies of files related to the error.
     * @param e Exception, Error, or Throwable
     * @param f1 Save a copy of this related file in its current state.
     */
    public CriticalError(Throwable e, File f1) {
        this(e);
        try {
            File fa = new File("Critical_error_file1.txt");
            Main.copyFile(f1, fa);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
