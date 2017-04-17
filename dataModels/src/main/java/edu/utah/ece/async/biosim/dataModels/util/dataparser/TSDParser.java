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
package edu.utah.ece.async.biosim.dataModels.util.dataparser;

import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 * 
 *
 * @author 
 * @author Chris Myers
 * @author <a href="http://www.async.ece.utah.edu/ibiosim#Credits"> iBioSim Contributors </a>
 * @version %I%
 */
public class TSDParser extends DataParser {

	private double minValue;
	private double maxValue;

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public TSDParser(String filename, boolean warn) {
		super(new ArrayList<String>(), new ArrayList<ArrayList<Double>>());

		// set the min and max such that they will be overwritten immediately
		minValue = Double.POSITIVE_INFINITY;
		maxValue = Double.NEGATIVE_INFINITY;

		try {
			warning = warn;
			boolean warning2 = warning;
			boolean warning3 = warning;
			FileInputStream fileInput = new FileInputStream(new File(filename));
			ProgressMonitorInputStream prog = new ProgressMonitorInputStream(component, "Reading Reb2sac Output Data From "
					+ new File(filename).getName(), fileInput);
			InputStream input = new BufferedInputStream(prog);

			boolean reading = true;
			char cha = 0;
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
					if (withinWord) {
						if (cha == '\"') {
							withinWord = false;
							readWord = false;
						}
						else {
							word += cha;
						}
					}
					else {
						if (Character.isWhitespace(cha) || cha == ',' || cha == '(') {
						}
						else if (cha == '\"') {
							withinWord = true;
						}
						else if (cha == ')') {
							moveToData = true;
						}
						else {
							readWord = false;
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
						if (word.equals("-inf")) {
							if (!warning3) {
								JOptionPane.showMessageDialog(component, "Found -INF in data." + "\nReplacing with " + (-1) * Double.MAX_VALUE + ".",
										"INF In Data", JOptionPane.WARNING_MESSAGE);
								warning3 = true;
							}
							word = "" + (-1) * Double.MAX_VALUE;
						}
						if (counter < species.size()) {
							insert = counter;
						}
						else {
							insert = counter % species.size();
						}
						word = word.replace("nan","NaN").replace("-NaN","NaN");
						double dataPoint = Double.parseDouble(word);
						(data.get(insert)).add(dataPoint);

						// add the value to the min and max if it isn't a time
						// value
						if (dataPoints > 0) {
							this.minValue = Math.min(this.minValue, dataPoint);
							this.maxValue = Math.max(this.maxValue, dataPoint);
						}

						counter++;
						dataPoints++;
						if (cha == ')') {
							
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
			if (Double.isInfinite(this.minValue) || Double.isInfinite(this.minValue)) {
				this.minValue = Double.NaN;
				this.maxValue = Double.NaN;
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(component, "Error Reading Data!" + "\nThere was an error reading the simulation output data.",
					"Error Reading Data", JOptionPane.ERROR_MESSAGE);
		}
	}
}
