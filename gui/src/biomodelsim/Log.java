package biomodelsim;

import java.awt.*;
import javax.swing.*;

public class Log extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5114950687877373374L;

	private JTextArea logArea; // log text area

	private JScrollPane scroll;

	public Log() {
		// sets up the log text area and menu
		logArea = new JTextArea();
		logArea.setEditable(false);
		scroll = new JScrollPane();
		scroll.setPreferredSize(new Dimension(1150, 150));
		scroll.setViewportView(logArea);
		this.add(scroll);
	}

	public void addText(String text) {
		logArea.append(text + "\n");
		logArea.setSelectionStart(logArea.getText().length());
		logArea.setSelectionEnd(logArea.getText().length());
	}

	public void resizePanel(int x, int y) {
		scroll.setPreferredSize(new Dimension(x, y));
	}
}
