package main;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

public class Log extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5114950687877373374L;

	private JTextArea logArea; // log text area
	
	private FileWriter logFile;

	private JScrollPane scroll;

	public Log() {
		// sets up the log text area and menu
		this.setPreferredSize(new Dimension(1150, 150));
		this.setLayout(new BorderLayout());
		logArea = new JTextArea();
		logArea.setEditable(false);
		scroll = new JScrollPane();
		scroll.setViewportView(logArea);
		this.add(scroll, "Center");
	}

	public void addText(String text) {
		logArea.append(text + "\n");
		logArea.setSelectionStart(logArea.getText().length());
		logArea.setSelectionEnd(logArea.getText().length());
		if (logFile != null) {
			try {
				logFile.write(text + "\n");
			}
			catch (Exception e) {
			}
		}
	}
	
	public void setLogFile(String filename) throws IOException {
		logFile = new FileWriter(new File(filename));
	}
	
	public void closeLogFile() throws IOException {
		logFile.close();
		logFile = null;
	}
}
