package edu.utah.ece.async.ibiosim.dataModels.graphData;

import java.awt.Shape;

public class GraphSpecies {
	
	private ShapeAndPaint sP;

	private boolean speciesFilled, speciesVisible, speciesConnected;

	private String runNumber, species, directory, xid, id;

	private int xnumber, number;

	public GraphSpecies(Shape s, String p, boolean filled, boolean visible, boolean connected, 
			String runNumber, String xid, String id, String species, int xnumber, int number, String directory) {
		sP = new ShapeAndPaint(s, p);
		this.speciesFilled = filled;
		this.speciesVisible = visible;
		this.speciesConnected = connected;
		this.runNumber = runNumber;
		this.species = species;
		this.xnumber = xnumber;
		this.number = number;
		this.directory = directory;
		this.xid = xid;
		this.id = id;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setXNumber(int xnumber) {
		this.xnumber = xnumber;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public void setPaint(String paint) {
		sP.setPaint(paint);
	}

	public void setShape(String shape) {
		sP.setShape(shape);
	}

	public void setVisible(boolean b) {
		speciesVisible = b;
	}

	public void setFilled(boolean b) {
		speciesFilled = b;
	}

	public void setConnected(boolean b) {
		speciesConnected = b;
	}

	public int getXNumber() {
		return xnumber;
	}

	public int getNumber() {
		return number;
	}

	public String getSpecies() {
		return species;
	}

	public ShapeAndPaint getShapeAndPaint() {
		return sP;
	}

	public boolean getFilled() {
		return speciesFilled;
	}

	public boolean getVisible() {
		return speciesVisible;
	}

	public boolean getConnected() {
		return speciesConnected;
	}

	public String getRunNumber() {
		return runNumber;
	}

	public String getDirectory() {
		return directory;
	}

	public String getXid() {
		return xid;
	}

	public String getID() {
		return id;
	}
}
