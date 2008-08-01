/**
 * 
 */
package gcm2sbml.visitor;

import gcm2sbml.network.BaseSpecies;
import gcm2sbml.network.BiochemicalSpecies;
import gcm2sbml.network.ConstantSpecies;
import gcm2sbml.network.DimerSpecies;
import gcm2sbml.network.NullSpecies;
import gcm2sbml.network.SpasticSpecies;
import gcm2sbml.network.SpeciesInterface;
import gcm2sbml.parser.GCMFile;

import java.util.Properties;

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
	 * @param biochemicalAbstraction
	 *            The biochemicalAbstraction to set.
	 */
	public void setBiochemicalAbstraction(boolean biochemicalAbstraction) {
		this.biochemicalAbstraction = biochemicalAbstraction;
	}

	/**
	 * @param cooperationAbstraction
	 *            The cooperationAbstraction to set.
	 */
	public void setCooperationAbstraction(boolean cooperationAbstraction) {
		this.cooperationAbstraction = cooperationAbstraction;
	}

	/**
	 * @param dimerizationAbstraction
	 *            The dimerizationAbstraction to set.
	 */
	public void setDimerizationAbstraction(boolean dimerizationAbstraction) {
		this.dimerizationAbstraction = dimerizationAbstraction;
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
	
	
	
	public void visitBaseSpecies(BaseSpecies specie) {}

	public void visitBiochemical(BiochemicalSpecies specie) {}

	public void visitConstantSpecies(ConstantSpecies specie) {}

	public void visitDimer(DimerSpecies specie) {}

	public void visitNullSpecies(NullSpecies specie) {}

	public void visitSpasticSpecies(SpasticSpecies specie) {}

	public void visitSpecies(SpeciesInterface specie) {}

	protected static GCMFile parameters = null;

	protected SBMLDocument document = null;

	protected boolean biochemicalAbstraction = false;

	protected boolean dimerizationAbstraction = false;

	protected boolean cooperationAbstraction = false;

}
