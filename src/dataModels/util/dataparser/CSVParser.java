/*******************************************************************************
 *  
 * This file is part of iBioSim. Please visit <http://www.async.ece.utah.edu/ibiosim>
 * for the latest version of iBioSim.
 *
 * Copyright (C) 2017 University of Utah
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the Apache License. A copy of the license agreement is provided
 * in the file named "LICENSE.txt" included with this software distribution
 * and also available online at <http://www.async.ece.utah.edu/ibiosim/License>.
 *  
 *******************************************************************************/
package dataModels.util.dataparser;

import java.io.*;
import java.util.*;

import javax.swing.*;

public class CSVParser extends DataParser {
	public CSVParser(String filename, boolean warn) {
		super(new ArrayList<String>(), new ArrayList<ArrayList<Double>>());
		try {
			warning = warn;
			boolean warning2 = warning;
			FileInputStream fileInput = new FileInputStream(new File(filename));
			ProgressMonitorInputStream prog = new ProgressMonitorInputStream(component, "Reading Reb2sac Output Data From "
					+ new File(filename).getName(), fileInput);
			InputStream input = new BufferedInputStream(prog);
			boolean reading = true;
			char cha = 0;
			boolean usingQuotes = false;
			while (reading) {
				String word = "";
				boolean readWord = true;
				boolean withinWord = false;
				boolean moveToData = false;
				while (readWord && !moveToData) {
					int read = input.read();
					if (read == -1) {
						reading = false;
						readWord = false;
					}
					cha = (char) read;
					// if (cha == '\n') {
					// moveToData = true;
					// }
					// else if (cha == ',') {
					// readWord = false;
					// }
					// else if (cha == ' ' && word.equals("")) {
					// }
					// else {
					// word += cha;
					// }
					if (withinWord) {
						if (usingQuotes) {
							if (cha == '\"') {
								withinWord = false;
								readWord = false;
								usingQuotes = false;
							}
							else {
								word += cha;
							}
						}
						else {
							if (cha == '\n') {
								moveToData = true;
							}
							else if (cha == ',') {
								withinWord = false;
								readWord = false;
							}
							else if (cha == ' ' && word.equals("")) {
							}
							else {
								word += cha;
							}
						}
					}
					else {
						if (cha == '\n') {
							moveToData = true;
						}
						else if (Character.isWhitespace(cha) || cha == ',') {
						}
						else if (cha == '\"') {
							usingQuotes = true;
							withinWord = true;
						}
						else {
							withinWord = true;
							word += cha;
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
				int dataPoints = 0;
				while (moveToData) {
					word = "";
					readWord = true;
					int read;
					while (readWord) {
						read = input.read();
						cha = (char) read;
						while (!Character.isWhitespace(cha) && cha != ',' && cha != ':' && cha != ';' && cha != '!' && cha != '?' && cha != '\"'
								&& cha != '\'' && cha != '(' && cha != ')' && cha != '{' && cha != '}' && cha != '[' && cha != ']' && cha != '<'
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
								JOptionPane.showMessageDialog(component, "Found NAN in data." + "\nReplacing with 0s.", "NAN In Data",
										JOptionPane.WARNING_MESSAGE);
								warning = true;
							}
							word = "0";
						}
						if (word.equals("inf")) {
							if (!warning2) {
								JOptionPane.showMessageDialog(component, "Found INF in data." + "\nReplacing with " + Double.MAX_VALUE + ".",
										"INF In Data", JOptionPane.WARNING_MESSAGE);
								warning2 = true;
							}
							word = "" + Double.MAX_VALUE;
						}
						if (counter < species.size()) {
							insert = counter;
						}
						else {
							insert = counter % species.size();
						}
						(data.get(insert)).add(Double.parseDouble(word));
						counter++;						
						dataPoints++;
						if (cha == '\n') {
							if (dataPoints > species.size()) {
								JOptionPane.showMessageDialog(component, "Time point includes more data than number of species", "Extra Data",
										JOptionPane.ERROR_MESSAGE);
								input.close();
								throw new ArrayIndexOutOfBoundsException();
							}
							if (dataPoints < species.size()) {
								JOptionPane.showMessageDialog(component, "Time point includes less data than number of species", "Missing Data",
										JOptionPane.ERROR_MESSAGE);
								input.close();
								throw new ArrayIndexOutOfBoundsException();
							}
							dataPoints = 0;
						}
					}
				}
			}
			input.close();
			prog.close();
			fileInput.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(component, "Error Reading Data!" + "\nThere was an error reading the simulation output data.",
					"Error Reading Data", JOptionPane.ERROR_MESSAGE);
		}
	}
}
