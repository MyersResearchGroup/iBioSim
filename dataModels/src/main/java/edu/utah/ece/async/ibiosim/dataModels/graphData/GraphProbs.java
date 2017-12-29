package edu.utah.ece.async.ibiosim.dataModels.graphData;

import java.awt.Paint;

public class GraphProbs {
	private Paint paint;

	private String species, directory, id, paintName;

	private int number;

	public GraphProbs(Paint paint, String paintName, String id, String species, int number, String directory) {
		this.paint = paint;
		this.paintName = paintName;
		this.species = species;
		this.number = number;
		this.directory = directory;
		this.id = id;
	}

	public Paint getPaint() {
		return paint;
	}

	public void setPaint(Paint p) {
		paint = p;
	}

	public String getPaintName() {
		return paintName;
	}

	public void setPaintName(String p) {
		paintName = p;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(String s) {
		species = s;
	}

	public String getDirectory() {
		return directory;
	}

	public String getID() {
		return id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int n) {
		number = n;
	}
}
