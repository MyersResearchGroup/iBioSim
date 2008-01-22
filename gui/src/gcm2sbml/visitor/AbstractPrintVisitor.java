/**
 * 
 */
package gcm2sbml.visitor;

import gcm2sbml.network.GeneticNetwork;

import java.util.ArrayList;

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

	protected SBMLDocument document = null;

	protected boolean biochemicalAbstraction = false;

	protected boolean dimerizationAbstraction = false;

	protected boolean cooperationAbstraction = false;

}
