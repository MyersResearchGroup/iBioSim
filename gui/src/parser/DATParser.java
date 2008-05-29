package parser;

import java.io.*;
import java.util.*;
import javax.swing.*;
import biomodelsim.*;

public class DATParser extends Parser {
	public DATParser(String filename, BioSim biosim) {
		try {
			component = biosim.frame();
			boolean warning = false;
			FileInputStream fileInput = new FileInputStream(new File(filename));
			ProgressMonitorInputStream prog = new ProgressMonitorInputStream(component,
					"Reading Reb2sac Output Data From " + new File(filename).getName(), fileInput);
			InputStream input = new BufferedInputStream(prog);
			species = new ArrayList<String>();
			data = new ArrayList<ArrayList<Double>>();
			boolean reading = true;
			char cha;
			while (reading) {
				String word = "";
				boolean readWord = true;
				boolean withinWord = false;
				boolean moveToData = false;
				boolean withinParens = false;
				while (readWord && !moveToData) {
					int read = input.read();
					if (read == -1) {
						reading = false;
						readWord = false;
					}
					cha = (char) read;
					if (withinWord) {
						if (cha == ')') {
							withinWord = false;
							readWord = false;
							withinParens = false;
						}
						else if (cha == ' ' && word.equals("")) {
						}
						else {
							word += cha;
						}
					}
					else {
						if (cha == '\n') {
							moveToData = true;
						}
						else if (Character.isWhitespace(cha) || cha == '#') {
						}
						else if (cha == '(') {
							withinParens = true;
						}
						else if (cha == ',' && withinParens) {
							withinWord = true;
						}
						else if (cha == ',' && !withinParens) {
						}
						else {
						}
					}
				}
				if (!word.equals("")) {
					species.add(word);
				}
				if (moveToData) {
					for (int i = 0; i < species.size(); i++) {
						data.add(new ArrayList<Double>());
					}
				}
				int counter = 0;
				while (moveToData) {
					word = "";
					readWord = true;
					int read;
					while (readWord) {
						read = input.read();
						cha = (char) read;
						while (!Character.isWhitespace(cha) && cha != ',' && cha != ':' && cha != ';'
								&& cha != '!' && cha != '?' && cha != '\"' && cha != '\'' && cha != '('
								&& cha != ')' && cha != '{' && cha != '}' && cha != '[' && cha != ']' && cha != '<'
								&& cha != '>' && cha != '_' && cha != '*' && cha != '=' && read != -1) {
							word += cha;
							read = input.read();
							cha = (char) read;
						}
						if (read == -1) {
							reading = false;
							moveToData = false;
						}
						readWord = false;
					}
					int insert;
					if (!word.equals("")) {
						if (word.equals("nan")) {
							if (!warning) {
								JOptionPane.showMessageDialog(component, "Found NAN in data."
										+ "\nReplacing with 0s.", "NAN In Data", JOptionPane.WARNING_MESSAGE);
								warning = true;
							}
							word = "0";
						}
						if (counter < species.size()) {
							insert = counter;
						}
						else {
							insert = counter % species.size();
						}
						(data.get(insert)).add(Double.parseDouble(word));
						counter++;
					}
				}
			}
			input.close();
			prog.close();
			fileInput.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(component, "Error Reading Data!"
					+ "\nThere was an error reading the simulation output data.", "Error Reading Data",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
