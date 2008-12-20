package parser;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import biomodelsim.*;

public class Parser {

	protected ArrayList<String> species;

	protected ArrayList<ArrayList<Double>> data;

	protected Component component;

	protected boolean warning;

	public Parser(ArrayList<String> species, ArrayList<ArrayList<Double>> data, BioSim biosim) {
		this.species = species;
		this.data = data;
		component = biosim.frame();
	}

	public ArrayList<String> getSpecies() {
		return species;
	}

	public ArrayList<ArrayList<Double>> getData() {
		return data;
	}

	public void setSpecies(ArrayList<String> species) {
		this.species = species;
	}

	public void setData(ArrayList<ArrayList<Double>> data) {
		this.data = data;
	}

	public boolean getWarning() {
		return warning;
	}

	public void outputTSD(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write("(");
			out.write("(");
			if (species.size() > 0) {
				out.write("\"" + species.get(0) + "\"");
			}
			for (int i = 1; i < species.size(); i++) {
				out.write(",\"" + species.get(i) + "\"");
			}
			out.write(")");
			for (int i = 0; i < data.get(0).size(); i++) {
				out.write(",(");
				if (data.size() > 0) {
					out.write("" + data.get(0).get(i));
				}
				for (int j = 1; j < data.size(); j++) {
					out.write("," + data.get(j).get(i));
				}
				out.write(")");
			}
			out.write(")");
			out.close();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(component, "Error Outputting Data Into A TSD File!",
					"Error Outputting Data", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void outputCSV(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			if (species.size() > 0) {
				out.write(species.get(0));
			}
			for (int i = 1; i < species.size(); i++) {
				out.write(", " + species.get(i));
			}
			for (int i = 0; i < data.get(0).size(); i++) {
				out.write("\n");
				if (data.size() > 0) {
					out.write("" + data.get(0).get(i));
				}
				for (int j = 1; j < data.size(); j++) {
					out.write(", " + data.get(j).get(i));
				}
			}
			out.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Error Outputting Data Into A TSD File!",
					"Error Outputting Data", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void outputDAT(String filename) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			out.write("#");
			if (species.size() > 0) {
				out.write("\"" + species.get(0) + "\"");
			}
			for (int i = 1; i < species.size(); i++) {
				out.write(" \"" + species.get(i) + "\"");
			}
			for (int i = 0; i < data.get(0).size(); i++) {
				out.write("\n");
				if (data.size() > 0) {
					out.write("" + data.get(0).get(i));
				}
				for (int j = 1; j < data.size(); j++) {
					out.write(" " + data.get(j).get(i));
				}
			}
			out.close();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(component, "Error Outputting Data Into A TSD File!",
					"Error Outputting Data", JOptionPane.ERROR_MESSAGE);
		}
	}
}
