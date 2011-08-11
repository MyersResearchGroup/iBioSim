
/**
 * 
 */
package gcm.visitor;

import gcm.network.AbstractionEngine;
import gcm.network.BaseSpecies;
import gcm.network.ComplexSpecies;
import gcm.network.ConstantSpecies;
import gcm.network.DiffusibleSpecies;
import gcm.network.Influence;
import gcm.network.NullSpecies;
import gcm.network.SpasticSpecies;
import gcm.network.SpeciesInterface;
import gcm.parser.GCMFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.SBMLDocument;

/**
 * Visitor that visits species and generates the species list in the SBML file
 * 
 * @author Nam
 * 
 */
public abstract class AbstractPrintVisitor implements SpeciesVisitor {

	public AbstractPrintVisitor(SBMLDocument document) {
		this.document = document;
	}
	
	/**
	 * @param dimerizationAbstraction
	 *            The dimerizationAbstraction to set.
	 */
	public void setComplexAbstraction(boolean complexAbstraction) {
		this.complexAbstraction = complexAbstraction;
	}
	
	/**
	 * Returns the property if it exists, else return the default value
	 * @param property the property to check
	 * @return the property if it exists, else return the default value
	 */
	public double getProperty(String key, Properties property, double defaultValue) {
		if (property.get(key) != null) {
			return Double.parseDouble(property.getProperty(key));
		} else {
			return defaultValue;
		}
	}
	
	public static void setGCMFile(GCMFile file) {
		parameters = file;
	}
	
	//Recursively breaks down repressing complex into its constituent species and complex formation equilibria
	protected String abstractComplex(String complexId, double multiplier) {
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, 0, r, kl);
		String expression = e.abstractComplex(complexId, multiplier, false);
		return expression;
	}
	
	protected String sequesterSpecies(String partId, double n) {
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, 0, r, kl);
		String expression = e.sequesterSpecies(partId, n, false);
		return expression;
	}
	
	protected String abstractDecay(String speciesId) {
		AbstractionEngine e = new AbstractionEngine(species, complexMap, partsMap, 0, r, kl);
		String expression = e.abstractDecay(speciesId);
		return expression;
	}
	
	public void visitBaseSpecies(BaseSpecies specie) {}

	public void visitComplex(ComplexSpecies specie) {}
	
	public void visitConstantSpecies(ConstantSpecies specie) {}

	public void visitNullSpecies(NullSpecies specie) {}

	public void visitSpasticSpecies(SpasticSpecies specie) {}

	public void visitSpecies(SpeciesInterface specie) {}
	
	//TODO-jstev: figure out what goes here
	public void visitDiffusibleSpecies(DiffusibleSpecies species) {}

	protected static GCMFile parameters = null;

	protected SBMLDocument document = null;
	
	protected boolean complexAbstraction = false;
	
	protected Reaction r;
	protected KineticLaw kl;
	protected HashMap<String, SpeciesInterface> species;
	protected HashMap<String, ArrayList<Influence>> complexMap;
	protected HashMap<String, ArrayList<Influence>> partsMap;
	
	
	
}
