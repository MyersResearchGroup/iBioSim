package gcm2sbml.network;

import java.util.ArrayList;
import java.util.Properties;

import gcm2sbml.util.GlobalConstants;
import gcm2sbml.visitor.SpeciesVisitor;

public class ComplexSpecies extends AbstractSpecies {
	
	public ComplexSpecies(SpeciesInterface s, ArrayList<PartSpecies> parts) {
		id = s.getId();
		this.parts = parts;
		properties = s.getProperties();
		for (PartSpecies p : parts) {
			size += (int) p.getStoich();
		}
	}
	
	/**
	 * Empty constructor
	 *
	 */
	public ComplexSpecies() {
		super();
	}
	
	public void accept(SpeciesVisitor visitor) {
		visitor.visitComplex(this);
	}
	
	
	public ArrayList<PartSpecies> getParts() {
		return parts;
	}
	
	
	public int getSize() {
		return size;
	}
	
	private ArrayList<PartSpecies> parts;
	private int size = 0;
}