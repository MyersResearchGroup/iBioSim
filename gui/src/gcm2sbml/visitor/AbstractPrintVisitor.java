/**
 * 
 */
package gcm2sbml.visitor;

import gcm2sbml.network.GeneticNetwork;
import gcm2sbml.parser.GCMFile;

import java.util.ArrayList;
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
	
	protected static GCMFile parameters = null;

	protected SBMLDocument document = null;

	protected boolean biochemicalAbstraction = false;

	protected boolean dimerizationAbstraction = false;

	protected boolean cooperationAbstraction = false;

}
