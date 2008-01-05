package biomodelsim;

import java.awt.*;
import javax.swing.*;

public class Log extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5114950687877373374L;

	private JTextArea logArea; // log text area

	public Log() {
		// sets up the log text area and menu
		logArea = new JTextArea();
		logArea.setEditable(false);
		JScrollPane scroll4 = new JScrollPane();
		scroll4.setPreferredSize(new Dimension(1150, 150));
		scroll4.setViewportView(logArea);
		this.add(scroll4);
	}

	public void addText(String text) {
		logArea.append(text + "\n");
		logArea.setSelectionStart(logArea.getText().length());
		logArea.setSelectionEnd(logArea.getText().length());
	}
}